package ua.hades7432.paleness;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {
    public InfuserScreen(InfuserMenu m,Inventory inv,Component name){
        super(m,inv,name);
        imageWidth=176;imageHeight=212;inventoryLabelY=118;
    }
    @Override protected void init(){
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("screen.paleness.infuse"),
            btn -> {if(minecraft!=null&&minecraft.gameMode!=null)
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
            }).bounds(leftPos+39,topPos+89,98,20).build());
    }
    private static void panel(GuiGraphics g,int x,int y,int w,int h,int inner){
        g.fill(x,y,x+w,y+h,0xFF584E50);
        g.fill(x+2,y+2,x+w-2,y+h-2,inner);
    }
    private static void slot(GuiGraphics g,int x,int y){
        g.fill(x-1,y-1,x+17,y+17,0xFF685C59);
        g.fill(x,y,x+16,y+16,0xFF30333B);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        panel(g,x,y,176,212,0xFFC9C5C1);
        panel(g,x+7,y+16,162,94,0xFF39383E);
        panel(g,x+9,y+124,158,85,0xFFA9A4A2);
        for(int yy=130;yy<=166;yy+=18)for(int xx=8;xx<=152;xx+=18)slot(g,x+xx,y+yy);
        for(int xx=8;xx<=152;xx+=18)slot(g,x+xx,y+188);
        slot(g,x+45,y+52);
        slot(g,x+113,y+52);
        g.fill(x+69,y+56,x+105,y+60,0xFF9F8C98);
        g.fill(x+98,y+51,x+105,y+65,0xFFDAD6E4);
        g.fill(x+16,y+77,x+160,y+83,0xFF21242B);
        if(menu.phase()==1){
            int bar=Math.min(140,menu.progress()*140/80);
            g.fill(x+18,y+79,x+18+bar,y+81,0xFFFAF9FF);
        }
        for(int px:new int[]{5,165})for(int py:new int[]{5,201})
            g.fill(x+px,y+py,x+px+4,y+py+4,0xFFB18E57);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,9,6,0xFFF5F5F5,false);
        g.drawString(font,Component.translatable("container.inventory"),8,119,0xFF373337,false);
        Component status=menu.phase()==1
            ? Component.translatable("screen.paleness.infusing")
            : menu.phase()==2 ? Component.translatable("tooltip.paleness.value",
                PalenessValues.format(menu.result()))
            : Component.translatable("screen.paleness.infuser_prompt");
        g.drawString(font,status,13,26,0xFFFFFFFF,false);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g,mx,my,partial);
        super.render(g,mx,my,partial);
        renderTooltip(g,mx,my);
    }
}