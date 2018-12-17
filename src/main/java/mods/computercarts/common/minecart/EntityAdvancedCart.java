package mods.computercarts.common.minecart;

import mods.computercarts.Settings;
import mods.computercarts.common.util.BitUtil;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

//Is the Base for a solid, self powered cart with a brake.
//Later I will add the Railcraft integration here
/*@Optional.InterfaceList({
    @Optional.Interface(iface = "mods.railcraft.api.carts.IEnergyTransfer", modid = "Railcraft"),
    @Optional.Interface(iface = "mods.railcraft.api.electricity.IElectricMinecart", modid = "Railcraft")
})*/
public abstract class EntityAdvancedCart extends EntityMinecart /*implements IEnergyTransfer, IElectricMinecart*/ {
    protected static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(EntityAdvancedCart.class, DataSerializers.BYTE);
    protected static final DataParameter<Float> ENGINE_SPEED = EntityDataManager.createKey(EntityAdvancedCart.class, DataSerializers.FLOAT);
    protected static final DataParameter<String> EMBLEM = EntityDataManager.createKey(EntityAdvancedCart.class, DataSerializers.STRING);

    //private ChargeHandler charge;

    public EntityAdvancedCart(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.setDisplayTile(Blocks.AIR.getDefaultState());
        this.setHasDisplayTile(false);
    }

    public EntityAdvancedCart(World world) {
        super(world);
        this.setDisplayTile(Blocks.AIR.getDefaultState());
        this.setHasDisplayTile(false);
    }

    protected void entityInit() {
        super.entityInit();

        /*if (Loader.isModLoaded("Railcraft") && FMLCommonHandler.instance().getEffectiveSide().isServer())
            charge = new ChargeHandler(this, ChargeHandler.Type.USER, Settings.ComputerCartETrackBuf, Settings.ComputerCartETrackLoss);*/

        this.dataManager.register(FLAGS, (byte)0);
        this.dataManager.register(ENGINE_SPEED, 0F);
        this.dataManager.register(EMBLEM, "");
        // Free DataWatcher 6-16, 23-32
    }

    protected final void setBrake(boolean brake) {
        this.dataManager.set(FLAGS, BitUtil.setBit(brake, this.dataManager.get(FLAGS), 0));
    }

    protected final boolean getBrake() {
        return BitUtil.getBit(this.dataManager.get(FLAGS), 0);
    }

    protected final void setEngineSpeed(double speed) {
        this.dataManager.set(ENGINE_SPEED, (float)speed);
    }

    protected final double getEngineSpeed() {
        return this.dataManager.get(ENGINE_SPEED);
    }

    public final boolean isLocked() {
        return BitUtil.getBit(this.dataManager.get(FLAGS), 1);
    }

    public final boolean isEngineActive() {
        return this.getEngineSpeed() != 0 && !this.isLocked() && !this.getBrake() && this.onRail();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("enginespeed", this.dataManager.get(ENGINE_SPEED));
        tag.setBoolean("brake", BitUtil.getBit(this.dataManager.get(FLAGS), 0));
        if (Loader.isModLoaded("Railcraft")) {
            NBTTagCompound rctag = new NBTTagCompound();
            rctag.setBoolean("locked", BitUtil.getBit(this.dataManager.get(FLAGS), 1));
            //if (this.charge != null) this.charge.writeToNBT(rctag);
            String emblem = this.dataManager.get(EMBLEM);
            if (!emblem.equals("")) rctag.setString("emblem_id", emblem);
            else rctag.removeTag("emblem_id");
            tag.setTag("railcraft", rctag);
        }
        nbt.setTag("advcart", tag);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey("advcart")) {
            NBTTagCompound tag = (NBTTagCompound) nbt.getTag("advcart");
            if (tag.hasKey("enginespeed")) this.dataManager.set(ENGINE_SPEED, (float) tag.getDouble("enginespeed"));
            if (tag.hasKey("brake"))
                this.dataManager.set(FLAGS, BitUtil.setBit(tag.getBoolean("brake"), this.dataManager.get(FLAGS), 0));
            if (tag.hasKey("railcraft") && Loader.isModLoaded("Railcraft")) {
                NBTTagCompound rctag = tag.getCompoundTag("railcraft");
                this.dataManager.set(FLAGS, BitUtil.setBit(rctag.getBoolean("locked"), this.dataManager.get(FLAGS), 1));
                //if (this.charge != null) this.charge.readFromNBT(rctag);
                if (rctag.hasKey("emblem_id")) {
                    String id = rctag.getString("emblem_id");
                    this.dataManager.set(EMBLEM, id);
                }
            }
        }
    }

    @Override
    public void killMinecart(DamageSource source) {
        this.setDead();
        ItemStack item = this.getCartItem();

        if (this.hasCustomName()) {
            item.setStackDisplayName(this.getCustomNameTag());
        }

        this.entityDropItem(item, 0F);
    }

    @Override
    public Type getType() {
        return null;
    }

    public boolean onRail() {
        int x = MathHelper.floor(this.posX);
        int y = MathHelper.floor(this.posY);
        int z = MathHelper.floor(this.posZ);
        return BlockRailBase.isRailBlock(this.world, new BlockPos(x, y, z));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) return;

        /*if (charge != null && Loader.isModLoaded("Railcraft")) {
            this.charge.tick();
            double mv = this.addEnergy(this.charge.getCharge() * Settings.OC_IC2PWR, true);  //Get max. energy we can load to the node
            mv = Math.min(mv, Settings.ComputerCartETrackLoad * Settings.OC_IC2PWR); //Check if the movable energy is higher than the limit.
            mv = this.charge.removeCharge(mv / Settings.OC_IC2PWR) * Settings.OC_IC2PWR; //Remove the charge from the buffer
            this.addEnergy(mv, false);    //Add the removed energy to the node network
        }*/
    }

    @Override
    protected void moveAlongTrack(BlockPos pos, IBlockState track) {
        super.moveAlongTrack(pos, track);
        if (this.world.isRemote) return;
        /*if (charge != null && Loader.isModLoaded("Railcraft")) {
            this.charge.tickOnTrack(pos);
        }*/
    }

    @Override
    protected void applyDrag() {
        if (!(BitUtil.getBit(this.dataManager.get(FLAGS), 0) || BitUtil.getBit(this.dataManager.get(FLAGS), 1))) {
            this.motionX *= 0.9699999785423279D;
            this.motionY *= 0;
            this.motionZ *= 0.9699999785423279D;

            if (this.dataManager.get(ENGINE_SPEED) != 0) {
                double yaw = this.rotationYaw * Math.PI / 180.0;

                this.motionX += Math.cos(yaw) * 10;
                this.motionZ += Math.sin(yaw) * 10;

                double nMotionX = Math.min(Math.abs(this.motionX), this.dataManager.get(ENGINE_SPEED));
                double nMotionZ = Math.min(Math.abs(this.motionZ), this.dataManager.get(ENGINE_SPEED));

                if (this.motionX < 0) this.motionX = -nMotionX;
                else this.motionX = nMotionX;

                if (this.motionZ < 0) this.motionZ = -nMotionZ;
                else this.motionZ = nMotionZ;
            }

            //Stop the cart if there is no speed. (below 0.0001 there are only sounds and no movement)
            if (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) < 0.0001) {
                this.motionX = 0;
                this.motionZ = 0;
            }
        } else if (!BitUtil.getBit(this.dataManager.get(FLAGS), 1)) {
            this.motionX = 0;
            this.motionZ = 0;
            this.setPosition(this.lastTickPosX, this.posY, this.lastTickPosZ);  // Fix: Bug on Booster Tracks (Reset Position)
        }
    }

    public double getSpeed() {
        return Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        if (Loader.isModLoaded("Railcraft") && Settings.GeneralFixCartBox)    //The Railcraft collision handler breaks some things
            return super.getEntityBoundingBox();
        return super.getCollisionBoundingBox();
    }

    public boolean canBePushed() {
        return (!BitUtil.getBit(this.dataManager.get(FLAGS), 0) || !onRail());
    }

    protected abstract double addEnergy(double amount, boolean simulate);

    /*-------Railcraft-------*/
    public void lockdown(boolean lock) {
        if (lock != isLocked())
            this.dataManager.set(FLAGS, BitUtil.setBit(lock, this.dataManager.get(FLAGS), 1));
    }

   /* @Override
    public boolean canExtractEnergy() {
        return false;
    }

    @Override
    public boolean canInjectEnergy() {
        return false;
    }

    @Override
    public double extractEnergy(Object arg0, double arg1, int arg2, boolean arg3, boolean arg4, boolean arg5) {
        return 0;
    }

    @Override
    public int getCapacity() {
        return (int) this.charge.getCapacity();
    }

    @Override
    public double getEnergy() {
        return charge.getCharge();
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public int getTransferLimit() {
        return (int) (Settings.ComputerCartETrackLoad * 1.1);
    }

    @Override
    public double injectEnergy(Object arg0, double arg1, int arg2, boolean arg3, boolean arg4, boolean arg5) {
        return 0;
    }

    @Override
    public ChargeHandler getChargeHandler() {
        return this.charge;
    }

    public boolean setEmblem(ItemStack stack) {
        if (!Loader.isModLoaded("Railcraft")) return false;
        return setEmblem(EmblemToolsServer.getEmblemIdentifier(stack));
    }

    public boolean setEmblem(String emblem) {
        if (!Loader.isModLoaded("Railcraft")) return false;
        if (Objects.equals(emblem, this.dataManager.get(EMBLEM))) return false;
        if (emblem == null) emblem = "";
        this.dataManager.set(EMBLEM, emblem);
        return true;
    }

    public String getEmblem() {
        if (!Loader.isModLoaded("Railcraft")) return null;
        return this.dataManager.get(EMBLEM);
    }

    @Optional.Method(modid = "Railcraft")
    public ResourceLocation getEmblemIcon() {
        String id = this.dataManager.get(EMBLEM);
        if (id.isEmpty()) return null;
        return EmblemToolsClient.packageManager.getEmblemTextureLocation(id);
    }*/

}
