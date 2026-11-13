package ua.hades7432.paleness;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final int DARK=0xFF24282C, METAL=0xFFC4BBB0, BORDER=0xFF766956,
            GOLD=0xFFAA854C, WHITE=0xFFFAF9F2, WINDOW=0xFF323338;
    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth=176;
        this.imageHeight=212;
        this.inventoryLabelY=118;
    }
    @Override protected void init() {
        super.init();
        int y=menu.analyzer?79:102;
        addRenderableWidget(Button.builder(Component.translatable(
                menu.analyzer ? "screen.paleness.analyze" : "screen.paleness.start"),
                button -> {
                    if (minecraft!=null && minecraft.gameMode!=null)
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                }).bounds(leftPos+37,topPos+y,102,20).build());
    }
    private static void panel(GuiGraphics g,int x,int y,int w,int h,int color) {
        g.fill(x,y,x+w,y+h,BORDER);
        g.fill(x+2,y+2,x+w-2,y+h-2,color);
        g.fill(x+3,y+3,x+w-3,y+4,0xFFD4C9B9);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        panel(g,leftPos,topPos,imageWidth,imageHeight,METAL);
        int x=leftPos,y=topPos;
        g.fill(x+5,y+5,x+171,y+112,0xFFAAA398);
        panel(g,x+8,y+15,160,menu.analyzer?61:84,DARK);
        if(menu.analyzer) {
            panel(g,x+66,y+38,45,35,WINDOW);
            panel(g,x+12,y+18,150,16,0xFF49443E);
            if(menu.state()==1) {
                // Keep the loading bar beside the sample slot, never over the scanning label.
                panel(g, x+14, y+51, 47, 15, WINDOW);
                int width=Math.min(41,menu.progress()*41/80);
                g.fill(x+17,y+56,x+17+width,y+61,WHITE);
            }
        } else {
            for(int[] p:new int[][] {{48,31},{110,31},{48,80},{110,80}}) {
                int cx=x+p[0]+8,cy=y+p[1]+8;
                g.fill(cx-2,cy-2,x+87,y+65,0xFFDDCFB1);
            }
            panel(g,x+72,y+50,32,32,0xFF524E58);
            if(menu.state()==1) {
                int radius=3+(menu.progress()/5)%11;
                int cx=x+87,cy=y+66;
                g.fill(cx-radius,cy-1,cx+radius,cy+1,WHITE);
                g.fill(cx-1,cy-radius,cx+1,cy+radius,WHITE);
            }
        }
        panel(g,x+5,y+124,166,85,0xFFAAA398);
        g.fill(x+5,y+183,x+171,y+185,BORDER);
        for (int sx=8;sx<=152;sx+=18) for(int sy=130;sy<=166;sy+=18) slotFrame(g,x+sx,y+sy);
        for (int sx=8;sx<=152;sx+=18) slotFrame(g,x+sx,y+188);
        if(menu.analyzer) slotFrame(g,x+79,y+51);
        else for(int[] p:new int[][]{{48,31},{110,31},{48,80},{110,80},{79,57}})slotFrame(g,x+p[0],y+p[1]);
        // Decorative brass bolts, matching the concept art.
        for(int px:new int[]{7,164})for(int py:new int[]{7,205})g.fill(x+px,y+py,x+px+5,y+py+5,GOLD);
    }
    private static void slotFrame(GuiGraphics g,int x,int y) {
        g.fill(x-1,y-1,x+17,y+17,0xFF655F59);
        g.fill(x,y,x+16,y+16,0xFF35373B);
    }
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY) {
        g.drawString(font,title,9,6,0xFFF7F2E8,false);
        g.drawString(font,Component.translatable("container.inventory"),8,119,0xFF393432,false);
        if(menu.state()==1) {
            g.drawString(font,Component.translatable(menu.analyzer ?
                "screen.paleness.scanning":"screen.paleness.condensing"),15,22,WHITE,false);
        } else if(menu.state()==2 || menu.value()!=0) {
            g.drawString(font,Component.translatable("tooltip.paleness.value",
                PalenessValues.format(menu.value())),15,22,WHITE,false);
        } else {
            g.drawString(font,Component.translatable(menu.analyzer?
                "screen.paleness.insert":"screen.paleness.insert_four"),15,22,WHITE,false);
        }
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial) {
        renderBackground(g,mouseX,mouseY,partial);
        super.render(g,mouseX,mouseY,partial);
        renderTooltip(g,mouseX,mouseY);
    }
}
