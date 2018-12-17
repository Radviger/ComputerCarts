package mods.computercarts.client.renderer.entity;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RenderComputerCart extends Render<EntityComputerCart> {

    /*private static final double EMBLEM_BX = 0.5001;
    private static final double EMBLEM_X = 0.5002;
    private static final boolean MOD_RAILCRAFT = Loader.isModLoaded("Railcraft");*/

    private static final ResourceLocation minecartTextures = new ResourceLocation(ComputerCarts.MODID + ":textures/entity/computercart.png");
    //private static final ResourceLocation emblemBackground = new ResourceLocation(ComputerCarts.MODID + ":textures/entity/computercart_eback.png");
    protected ModelComputerCart modelMinecart = new ModelComputerCart();

    public RenderComputerCart(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityComputerCart cart, double x, double y, double z, float rotation, float partialTicks) {
        GlStateManager.pushMatrix();
        this.bindEntityTexture(cart);

        double cx = cart.lastTickPosX + (cart.posX - cart.lastTickPosX) * (double) partialTicks;
        double cy = cart.lastTickPosY + (cart.posY - cart.lastTickPosY) * (double) partialTicks;
        double cz = cart.lastTickPosZ + (cart.posZ - cart.lastTickPosZ) * (double) partialTicks;
        double d6 = 0.30000001192092896D;
        double ryaw = (cart.rotationYaw + 360D) % 360;

        float yaw = rotation;
        float pitch = cart.rotationPitch;

        Vec3d vec1 = cart.getPosOffset(cx, cy, cz, d6);
        Vec3d vec2 = cart.getPosOffset(cx, cy, cz, -d6);

        if (vec1 != null && vec2 != null) {
            y += (vec1.y + vec2.y) / 2 - cy;
            Vec3d vec3 = vec2.addVector(-vec1.x, -vec1.y, -vec1.z);
            if (vec3.lengthVector() != 0) {
                yaw = (float) (Math.atan2(vec3.z, vec3.x) * 180 / Math.PI);
                pitch = (float) (Math.atan(vec3.y) * 73);
            }
        }

        yaw = (yaw + 360F) % 360F;
        ryaw = yaw - ryaw;
        if (ryaw <= -90 || ryaw >= 90) {
            yaw += 180D;
            pitch *= -1;
        }
        yaw = 90F - yaw;

        GlStateManager.translate((float) x, (float) y + 0.4, (float) z);
        GlStateManager.rotate(yaw, 0, 1, 0);
        GlStateManager.rotate(-pitch, 1, 0, 0);
        float rollamp = (float) cart.getRollingAmplitude() - partialTicks;
        float dmgamp = cart.getDamage() - partialTicks;

        if (dmgamp < 0F)
            dmgamp = 0F;

        if (rollamp > 0F) {
            GlStateManager.rotate(MathHelper.sin(rollamp) * rollamp * dmgamp / 10F * (float) cart.getRollingDirection(), 0F, 0F, 1F);
        }

        GlStateManager.scale(-1F, -1F, 1F);

        this.modelMinecart.renderTile(cart, 0.0625F);

        GlStateManager.rotate(90F, 0, 1, 0);

        /*ResourceLocation emblem = (MOD_RAILCRAFT) ? cart.getEmblemIcon() : null;

        if (emblem != null) {
            Tessellator tes = Tessellator.getInstance();
            this.bindTexture(emblemBackground);

            BufferBuilder buff = tes.getBuffer();

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.pos((3D / 16D) + (6D / 16D), (5D / 16D), -EMBLEM_BX).tex(0, 1).endVertex();
            buff.pos((3D / 16D) + (6D / 16D), 0, -EMBLEM_BX).tex(0, 0).endVertex();
            buff.pos((3D / 16D), 0, -EMBLEM_BX).tex(1, 0).endVertex();
            buff.pos((3D / 16D), (5D / 16D), -EMBLEM_BX).tex(1, 1).endVertex();
            tes.draw();

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.pos((3D / 16D), (5D / 16D), EMBLEM_BX).tex(1, 1).endVertex();
            buff.pos((3D / 16D), 0, EMBLEM_BX).tex(1, 0).endVertex();
            buff.pos((3D / 16D) + (6D / 16D), 0, EMBLEM_BX).tex(0, 0).endVertex();
            buff.pos((3D / 16D) + (6D / 16D), (5D / 16D), EMBLEM_BX).tex(0, 1).endVertex();
            tes.draw();

            Minecraft.getMinecraft().renderEngine.bindTexture(emblem);    //Render the actual emblem

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.pos((4D / 16D) + (5D / 16D), (5D / 16D), -EMBLEM_X).tex(1, 1).endVertex();
            buff.pos((4D / 16D) + (5D / 16D), 0, -EMBLEM_X).tex(1, 0).endVertex();
            buff.pos((4D / 16D), 0, -EMBLEM_X).tex(0, 0).endVertex();
            buff.pos((4D / 16D), (5D / 16D), -EMBLEM_X).tex(0, 1).endVertex();
            tes.draw();

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.pos((4D / 16D), (5D / 16D), EMBLEM_X).tex(1, 1).endVertex();
            buff.pos((4D / 16D), 0, EMBLEM_X).tex(1, 0).endVertex();
            buff.pos((4D / 16D) + (5D / 16D), 0, EMBLEM_X).tex(0, 0).endVertex();
            buff.pos((4D / 16D) + (5D / 16D), (5D / 16D), EMBLEM_X).tex(0, 1).endVertex();
            tes.draw();
        }*/

        GlStateManager.popMatrix();
    }


    @Override
    protected ResourceLocation getEntityTexture(EntityComputerCart cart) {
        return minecartTextures;
    }

}
