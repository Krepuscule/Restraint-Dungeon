package com.twi.restraint_dungeon.event.mod_event.player_leash;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerLeashData;
import com.twi.restraint_dungeon.utils.mod_utils.leash.PlayerLeashUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientLeashRenderHandler {

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderLivingEvent.Post<?, ?> event) {
        if (!(event.getEntity() instanceof Player leashedPlayer)) return;

        PlayerLeashData data = leashedPlayer.getData(ModAttachments.PLAYER_LEASH.get());
        if (!data.hasLeash()) return;

        Entity leasher = PlayerLeashUtils.getLeashHolder(leashedPlayer);
        if (leasher == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        float partialTicks = event.getPartialTick();

        Vec3 leasherRopePos = leasher.getRopeHoldPosition(partialTicks);

        double bodyRotRad = (double)(leashedPlayer.getPreciseBodyRotation(partialTicks) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vec3 playerOffset = leashedPlayer.getLeashOffset(partialTicks).add(0.0D, -0.4D, 0.0D); // 下移至颈部

        double leashOffsetX = Math.cos(bodyRotRad) * playerOffset.z + Math.sin(bodyRotRad) * playerOffset.x;
        double leashOffsetZ = Math.sin(bodyRotRad) * playerOffset.z - Math.cos(bodyRotRad) * playerOffset.x;

        double pX = Mth.lerp((double)partialTicks, leashedPlayer.xo, leashedPlayer.getX()) + leashOffsetX;
        double pY = Mth.lerp((double)partialTicks, leashedPlayer.yo, leashedPlayer.getY()) + playerOffset.y;
        double pZ = Mth.lerp((double)partialTicks, leashedPlayer.zo, leashedPlayer.getZ()) + leashOffsetZ;

        poseStack.pushPose();

        poseStack.translate(leashOffsetX, playerOffset.y, leashOffsetZ);
        Matrix4f matrix = poseStack.last().pose();

        float f = (float)(leasherRopePos.x - pX);
        float f1 = (float)(leasherRopePos.y - pY);
        float f2 = (float)(leasherRopePos.z - pZ);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.leash());

        float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;

        BlockPos playerEyePos = BlockPos.containing(leashedPlayer.getEyePosition(partialTicks));
        BlockPos leasherEyePos = BlockPos.containing(leasher.getEyePosition(partialTicks));

        int blockLightPlayer = leashedPlayer.isOnFire() ? 15 : leashedPlayer.level().getBrightness(LightLayer.BLOCK, playerEyePos);
        int blockLightLeasher = leasher.isOnFire() ? 15 : leasher.level().getBrightness(LightLayer.BLOCK, leasherEyePos);
        int skyLightPlayer = leashedPlayer.level().getBrightness(LightLayer.SKY, playerEyePos);
        int skyLightLeasher = leasher.level().getBrightness(LightLayer.SKY, leasherEyePos);

        for (int i1 = 0; i1 <= 24; ++i1) {
            addVertexPair(vertexConsumer, matrix, f, f1, f2, blockLightPlayer, blockLightLeasher, skyLightPlayer, skyLightLeasher, 0.025F, 0.025F, f5, f6, i1, false);
        }

        for (int j1 = 24; j1 >= 0; --j1) {
            addVertexPair(vertexConsumer, matrix, f, f1, f2, blockLightPlayer, blockLightLeasher, skyLightPlayer, skyLightLeasher, 0.025F, 0.0F, f5, f6, j1, true);
        }

        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer consumer, Matrix4f matrix, float dx, float dy, float dz,
                                      int blPlayer, int blLeasher, int slPlayer, int slLeasher,
                                      float p_352293_, float p_352138_, float p_352315_, float p_352162_,
                                      int step, boolean isReversePass) {
        float f = (float)step / 24.0F;
        int i = (int)Mth.lerp(f, (float)blPlayer, (float)blLeasher);
        int j = (int)Mth.lerp(f, (float)slPlayer, (float)slLeasher);
        int k = LightTexture.pack(i, j);

        float f1 = step % 2 == (isReversePass ? 1 : 0) ? 0.7F : 1.0F;
        float r = 0.5F * f1;
        float g = 0.4F * f1;
        float b = 0.3F * f1;

        float xPos = dx * f;
        float yPos = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
        float zPos = dz * f;

        consumer.addVertex(matrix, xPos - p_352315_, yPos + p_352138_, zPos + p_352162_).setColor(r, g, b, 1.0F).setLight(k);
        consumer.addVertex(matrix, xPos + p_352315_, yPos + p_352293_ - p_352138_, zPos - p_352162_).setColor(r, g, b, 1.0F).setLight(k);
    }
}