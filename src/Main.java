import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.util.FactoryTemplates;
import org.opencv.core.Core;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;

import javax.swing.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;


public class Main {

    private static final String ABSORB = "absorb.jpg";
    private static final String SKILL_TAB = "skilltab.jpg";

    private static final Region INVENTORY = new Region(1080, 570, 200, 260);
    private static final Region SKILLTAB = new Region(1100, 535, 30, 45);
    private static final Region ABSORB_REGION = new Region(120, 140, 30, 30);

    private static final Region BACKPACK = new Region(1157, 534, 40, 40);

    private static final MouseMotionFactory factory = FactoryTemplates.createAverageComputerUserMotionFactory();

    private static Date lastClick;

    public static void main(String[] args) throws FindFailed, InterruptedException, AWTException {
        //settings
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        ImagePath.add(System.getProperty("user.dir"));
        Settings.MinSimilarity = 0.4;

        //highlight regions to align client
        ABSORB_REGION.highlightOn();
        INVENTORY.highlightOn();
        SKILLTAB.highlightOn();
        BACKPACK.highlightOn();
        JOptionPane.showConfirmDialog(null, "Ready?", "Ready?", JOptionPane.YES_NO_OPTION);
        ABSORB_REGION.highlightOff();
        INVENTORY.highlightOff();
        SKILLTAB.highlightOff();
        BACKPACK.highlightOff();
        //run test hover
        hover();

        lastClick = new Date();
        while (true) {

            System.out.println("heal check");
            if (needHeal()) {
                if (!inventoryOpen()) {
                    openInventoryTab();
                }
                randSleep(1000, 2198);
                if (inventoryOpen()) {
                    drinkAbsorption();
                } else {
                    System.out.println("########## FAILED TO OPEN INVENTORY ##########");
                }
            }

            System.out.println("timeout check");
            int randDuration = ThreadLocalRandom.current().nextInt(8, 12 + 1);
            long MAX_DURATION = MILLISECONDS.convert(randDuration, MINUTES);
            Date now = new Date();
            long duration = now.getTime() - lastClick.getTime();
            if (duration >= MAX_DURATION) {
                System.out.println("######### randDuration passed #########");
                hover();
            }
            System.out.println("sleep");
            randSleep(5000,25000);
        }



    }

    public static Match searchRegionOne(Region curRegion, String item, double similarity) {
        try {
            Match t = curRegion.find(item);
            if (t==null) return null;
            System.out.println(item + ":" + t.getScore());
            if(t.getScore() > similarity) {
                return t;
            }
        } catch (FindFailed e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void openInventoryTab() {
        try {

            Match open = searchRegionOne(BACKPACK,"inventorytab.jpg",0.5);
            if (open != null) {
                move(open.getCenter(), true);
                //open.doubleClick();
            }
        } catch (Exception ex) {
            System.out.println("openInventoryTab failure err" + ex.getMessage());
        }
        System.out.println("openInventoryTab Failure - false");
    }
    public static boolean inventoryOpen() {
        try {
        Match exists = searchRegionOne(INVENTORY,"emptyslot.jpg",0.6);
        if (exists != null) {
            return true;
        } else {
            return false;
        }
        } catch ( Exception ex) {
            System.out.println("InventoryOpen failure err" + ex.getMessage());
            return false;
        }
    }


    public static void openSKillTab() {
        try {

            Match open = searchRegionOne(SKILLTAB,SKILL_TAB,0.3);
            if (open != null) {
                //open.doubleClick();
                move(open.getCenter(), true);
            }
        } catch (Exception ex) {
            System.out.println("openSkillTab failure err" + ex.getMessage());
        }
        System.out.println("openSkillTab Failure - false");
    }
    public static boolean skillTabOpen() {
        try {
            Match exists = searchRegionOne(INVENTORY, "strengthskill.jpg", 0.6);
            if (exists != null) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            return false;
        }
    }



    public static void hover() throws InterruptedException, AWTException {
        System.out.println("rand hover init");
        int num = ThreadLocalRandom.current().nextInt(1, 20 + 1);
        String img = "";
        if (num >= 1 && num < 5) img = "attackskill.jpg";
        if (num >= 5 && num < 10) img = "strengthskill.jpg";
        if (num >= 10 && num <= 20) img = "defenceskill.jpg";

        if (!skillTabOpen()) {
            System.out.println("opening skill tab");
            openSKillTab();
            randSleep(600,1700);
        }
        if (skillTabOpen()){
            System.out.println("Skill tab open");
            System.out.println("finding: " + img);
            Match skill = searchRegionOne(INVENTORY, img, 0.4);
            if (skill != null) {
                System.out.println("found: " + img + ", hovering");
                move(skill.getCenter(), false);
                randSleep(3,8);
                if (!inventoryOpen()) {
                    openInventoryTab();
                }
            }
        }

    }

    public static boolean needHeal() {
        try {
            Match yes = searchRegionOne(ABSORB_REGION,"red.jpg",0.6);
            if (yes != null) {
                return true;
            }
            Match no = searchRegionOne(ABSORB_REGION,"pink.jpg",0.6);
            if (no != null) {
                return false;
            }
        } catch (Exception ex) {
            System.out.println("needHeal failure err");
            return false;
        }
        System.out.println("needHeal Failure - false");
        return false;
    }

    public static void drinkAbsorption() {
        try {

            int repeats = ThreadLocalRandom.current().nextInt(3, 7 + 1);
            System.out.println("drinking amount: " + repeats);
            for (int i = 0; i < repeats; i++){
                Match exists = searchRegionOne(INVENTORY,ABSORB,0.6);
                if (exists != null) {
                    System.out.println("drinking");
                    move(exists.getCenter(), true);
                    randSleep(500,1200);
                } else {
                    System.out.println("couldn't find absorbtions");
                }
            }

        } catch ( Exception ex) {
            System.out.println("drinkabsorbtions failure err" + ex.getMessage());
        }
    }

    public static void randSleep(int min, int max) throws InterruptedException {
        MILLISECONDS.sleep( ThreadLocalRandom.current().nextInt(min, max + 1));
    }

    public static void move(Location location, boolean lclick) throws InterruptedException, AWTException {
        int xdistort = ThreadLocalRandom.current().nextInt(1, 4 + 1);
        int ydistort = ThreadLocalRandom.current().nextInt(1, 4 + 1);
        System.out.println("distorted click by: " + xdistort + "/" + ydistort);
        xdistort = xdistort + location.getX();
        ydistort = ydistort + location.getY();

        Location offset = new Location(xdistort,ydistort);
        System.out.println("moving to: " + offset.getX() + "/" + offset.getY() + " click: " + lclick);
        factory.move(offset.getX(), offset.getY());
        randSleep(354, 682);
        if (!lclick) return;
        if (!offset.equals(Mouse.at())) {
            System.out.println("missmatch location - " + "location provided: " + offset.getX() + "/" + offset.getY() + " Mouse Location: " + Mouse.at().getX() + "/" + Mouse.at().getY());
        return;
        }
        lastClick = new Date();
        Robot bot = new Robot();
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        randSleep(82,240);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

}