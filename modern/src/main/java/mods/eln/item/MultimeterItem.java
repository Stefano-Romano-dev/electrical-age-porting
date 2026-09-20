package mods.eln.item;

import mods.eln.ElectricalAge;
import mods.eln.node.six.LegacyElectricalMeasurementFormatter;
import mods.eln.node.six.SixNodeBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** First modern measuring tool, preserving the legacy chat-based multimeter interaction. */
public final class MultimeterItem extends Item {
    public MultimeterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof SixNodeBlockEntity host)
                || !host.contentsSnapshot().containsKey(context.getClickedFace())) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        var lifecycle = ElectricalAge.simulationLifecycle();
        var graph = lifecycle == null ? null : lifecycle.getGraph(level);
        var measurement = graph == null
                ? null
                : graph.measurementAt(context.getClickedPos(), context.getClickedFace());
        if (measurement != null) {
            String formatted = LegacyElectricalMeasurementFormatter.format(measurement);
            context.getPlayer().sendSystemMessage(Component.literal(formatted));
            ElectricalAge.LOGGER.debug(
                    "Multimeter reading at {} on {}: {}", context.getClickedPos(), context.getClickedFace(), formatted);
        }
        return InteractionResult.SUCCESS;
    }
}
