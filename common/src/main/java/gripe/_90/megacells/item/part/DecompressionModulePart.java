package gripe._90.megacells.item.part;

import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;

import gripe._90.megacells.MEGACells;
import gripe._90.megacells.misc.DecompressionPattern;
import gripe._90.megacells.misc.DecompressionService;

public class DecompressionModulePart extends AEBasePart implements ICraftingProvider, IGridTickable {
    @PartModels
    public static final IPartModel MODEL = new PartModel(MEGACells.makeId("part/decompression_module"));

    private final Object2LongMap<AEKey> outputs = new Object2LongOpenHashMap<>();

    public DecompressionModulePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .addService(ICraftingProvider.class, this)
                .setIdlePowerUsage(10.0);
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        var grid = getMainNode().getGrid();
        return grid != null ? grid.getService(DecompressionService.class).getPatterns() : List.of();
    }

    @Override
    public int getPatternPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!getMainNode().isActive() || !(patternDetails instanceof DecompressionPattern pattern)) {
            return false;
        }

        var output = pattern.getPrimaryOutput();
        outputs.mergeLong(output.what(), output.amount(), Long::sum);

        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(3, 3, 12, 13, 13, 16);
        bch.addBox(5, 5, 11, 11, 11, 12);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODEL;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, outputs.isEmpty(), true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var storage = node.getGrid().getStorageService().getInventory();

        for (var output : outputs.object2LongEntrySet()) {
            var what = output.getKey();
            var amount = output.getLongValue();
            var inserted = storage.insert(what, amount, Actionable.MODULATE, IActionSource.ofMachine(this));

            if (inserted >= amount) {
                outputs.removeLong(what);
            } else if (inserted > 0) {
                outputs.put(what, amount - inserted);
            }
        }

        return TickRateModulation.URGENT;
    }
}
