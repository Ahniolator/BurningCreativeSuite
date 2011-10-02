package me.ahniolator.plugins.burningcreativesuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.bukkit.inventory.ItemStack;

public class BCSInventoryManager {

    public File file;
    public String dir;
    public ItemStack[] stack;
    public int itemIDH = 0;
    public int amountH = 0;
    public byte dataH = 0;
    public short durabilityH = 0;
    public Object obj;

    public String toString(ItemStack[] stack, int x) {
        this.itemIDH = stack[x].getTypeId();
        this.amountH = stack[x].getAmount();
        this.durabilityH = stack[x].getDurability();
        if (stack[x].getData() == null) {
            return this.itemIDH + "," + this.amountH + "," + "!" + "," + this.durabilityH;
        } else {
            this.dataH = stack[x].getData().getData();
        }
        return this.itemIDH + "," + this.amountH + "," + this.dataH + "," + this.durabilityH;
    }

    public ItemStack toItemStack(String string, ItemStack stack) {
        ItemStack newstack = new ItemStack(0);
        try {
            String[] dataArray = string.split(",");
            if (stack == null) {
                stack = new ItemStack(0);
            }
            if (dataArray.length == 4) {
                if (dataArray[0].equals("!") || dataArray[1].equals("!") || dataArray[3].equals("!")) {
                    newstack = null;
                    return newstack;
                }
                int itemID = (Integer.valueOf(Integer.parseInt(dataArray[0]))).intValue();
                int amount = (Integer.valueOf(Integer.parseInt(dataArray[1]))).intValue();
                short durability = (Short.valueOf(Short.parseShort(dataArray[3]))).shortValue();
                byte data = 0;
                if (dataArray[0] == null || dataArray[1] == null || dataArray[3] == null) {
                    newstack = null;
                    return newstack;
                }
                if (dataArray[2].equalsIgnoreCase("!")) {
                    newstack = new ItemStack(itemID, amount, durability);
                    return newstack;
                } else {
                    data = (Byte.valueOf(Byte.parseByte(dataArray[2]))).byteValue();
                }
                if (stack == null) {
                    System.out.println("Error: Stack is null!");
                    newstack = null;
                    return newstack;
                }
                newstack = new ItemStack(itemID, amount, durability, data);
                return newstack;
            }
            return newstack;
        } catch (Exception e) {
            e.printStackTrace();
            return newstack;
        }
    }

    public void saveFile(Object obj, File file) {
        if (file.exists()) {
            String dirs = file.getPath();
            file.delete();
            this.createNewFile(file, dirs);
        }
        try {
            this.obj = obj;
            this.file = file;
            this.saveFile(obj, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createNewFile(File file, String dir) {
        if (file == null) {
            System.out.println("Error: File is null");
        }
        if (dir == null) {
            System.out.println("Error: Dir is null");
        }
        if (file == null || dir == null) {
            return;
        }
        this.file = file;
        this.dir = dir;
        try {
            new File(this.dir).mkdirs();
            this.file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public void saveString(String[] string, File file, String path) throws Exception {
        if (file.exists()) {
            file.delete();
        }
        createNewFile(file, path);
        FileOutputStream buf = new FileOutputStream(file, true);
        ObjectOutputStream oos = new ObjectOutputStream(buf);
        oos.writeObject(string);
        oos.flush();
        oos.close();
    }

    public static String[] loadStringArray(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        String[] string = (String[]) ois.readObject();
        ois.close();
        return string;
    }

    public static void save(Object obj, File file) throws Exception {
        String path = file.toString();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(obj);
        oos.flush();
        oos.close();
    }

    public static Object load(File file) throws Exception {
        String path = file.toString();
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
        Object result = ois.readObject();
        ois.close();
        return result;
    }
}