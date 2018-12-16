package mods.computercarts.client.renderer.entity;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ComputerCartRenderer extends Render<EntityComputerCart> {

    /*private static final double EMBLEM_BX = 0.5001;
    private static final double EMBLEM_X = 0.5002;
    private static final boolean MOD_RAILCRAFT = Loader.isModLoaded("Railcraft");*/

    private static final ResourceLocation minecartTextures = new ResourceLocation(ComputerCarts.MODID + ":textures/entity/computercart.png");
    //private static final ResourceLocation emblemBackground = new ResourceLocation(ComputerCarts.MODID + ":textures/entity/computercart_eback.png");
    protected ComputerCartModel modelMinecart = new ComputerCartModel();

    public ComputerCartRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityComputerCart cart, double x, double y, double z, float rotation, float partialTicks) {
        GL11.glPushMatrix();
        this.bindEntityTexture(cart);

        double cx = cart.lastTickPosX + (cart.posX - cart.lastTickPosX) * (double) partialTicks;
        double cy = cart.lastTickPosY + (cart.posY - cart.lastTickPosY) * (double) partialTicks;
        double cz = cart.lastTickPosZ + (cart.posZ - cart.lastTickPosZ) * (double) partialTicks;
        double d6 = 0.30000001192092896D;
        double ryaw = (cart.rotationYaw + 360D) % 360;

        double yaw = rotation;
        float pitch = cart.rotationPitch;

        Vec3d vec1 = cart.getPosOffset(cx, cy, cz, d6);
        Vec3d vec2 = cart.getPosOffset(cx, cy, cz, -d6);

        if (vec1 != null && vec2 != null) {
            y += (vec1.y + vec2.y) / 2.0D - cy;
            Vec3d vec3 = vec2.addVector(-vec1.x, -vec1.y, -vec1.z);
            if (vec3.lengthVector() != 0) {
                yaw = (float) (Math.atan2(vec3.z, vec3.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(vec3.y) * 73.0D);
            }
        }

        yaw = (yaw + 360D) % 360D;
        ryaw = yaw - ryaw;
        if (ryaw <= -90 || ryaw >= 90) {
            yaw += 180D;
            pitch *= -1;
        }
        yaw = 90F - yaw;

        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glRotated(yaw, 0.0D, 1.0D, 0.0D);
        GL11.glRotatef(-pitch, 1.0F, 0.0F, 0.0F);
        float rollamp = (float) cart.getRollingAmplitude() - partialTicks;
        float dmgamp = cart.getDamage() - partialTicks;

        if (dmgamp < 0.0F)
            dmgamp = 0.0F;

        if (rollamp > 0.0F) {
            GL11.glRotatef(MathHelper.sin(rollamp) * rollamp * dmgamp / 10.0F * (float) cart.getRollingDirection(), 0.0F, 0.0F, 1.0F);
        }

        GL11.glColor3f(1, 1, 1);
        GL11.glScalef(-1.0F, -1.0F, 1.0F);
        this.modelMinecart.renderTile(cart, 0.0625F);

        GL11.glRotated(90D, 0.0D, 1.0D, 0.0D);

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

        GL11.glPopMatrix();
    }


    @Override
    protected ResourceLocation getEntityTexture(EntityComputerCart cart) {
        return minecartTextures;
    }

}
