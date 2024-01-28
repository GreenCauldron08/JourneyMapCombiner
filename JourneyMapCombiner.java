import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class JourneyMapCombiner {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("There should be at least 3 arguments: 2 (or more) source directory paths and 1 destination directory path.");
            System.exit(1);
        }

        File[] worlds = new File[args.length - 1];
        ArrayList<File> dimensions = new ArrayList<File>();
        for (int x = 0; x < worlds.length; x++) {
            worlds[x] = new File(args[x]);
            File[] subList = worlds[x].listFiles();
            for (File subFile : subList) {
                if (subFile.isDirectory() && !subFile.getName().startsWith("waypoints")) {//only the folders we want
                    dimensions.add(subFile);
                }
            }
        }

        //now we have our dimension Files
        ArrayList<String> dimensionPaths = getUniquePaths(dimensions, null);//get all the dimension paths
        ArrayList<File> levels = new ArrayList<File>();
        for (File dim : dimensions) {
            levels.addAll(Arrays.asList(dim.listFiles()));//assuming there's no extra files
        }

        //now we have our level Files
        ArrayList<String> levelPaths = getUniquePaths(levels, dimensionPaths);//get all level paths
        ArrayList<File> images = new ArrayList<File>();
        for (File level : levels) {
            images.addAll(Arrays.asList(level.listFiles()));
        }

        //now we have our image files
        ArrayList<String> imagePaths = getUniquePaths(images, dimensionPaths);//get all image paths
        for (String levelPath : levelPaths) {
            File outDir = new File(args[args.length - 1] + File.separator + levelPath);
            outDir.mkdirs();
        }

        for (String imagePath : imagePaths) {//the main loop
            if (!imagePath.endsWith(".png"))
                continue;

            System.out.println(imagePath);//so user knows what it's doing
            ArrayList<File> comparables = new ArrayList<File>();
            for (File f : images) {
                if (f.getPath().endsWith(imagePath)) {
                    comparables.add(f);
                }
            }

            boolean moreSorting = true;
            while (moreSorting) {//bubble sort
                moreSorting = false;
                for (int x = 1; x < comparables.size(); x++) {
                    if (comparables.get(x - 1).lastModified() > comparables.get(x).lastModified()) {
                        moreSorting = true;
                        File temp = comparables.get(x);
                        comparables.set(x, comparables.get(x - 1));
                        comparables.set(x - 1, temp);
                    }
                }
            }

            //now comparables is sorted oldest to newest (by date last modified), so the newer map is drawn overtop the old
            BufferedImage buffI = ImageIO.read(comparables.get(0));
            Graphics2D g2 = buffI.createGraphics();
            for (int x = 1; x < comparables.size(); x++) {
                g2.drawImage(ImageIO.read(comparables.get(x)), 0, 0, null);
            }
            g2.dispose();

            File outFile = new File(args[args.length - 1] + File.separator + imagePath);
            ImageIO.write(buffI, "png", outFile);
            outFile.setLastModified(comparables.get(comparables.size() - 1).lastModified());
        }
    }

    public static ArrayList<String> getUniquePaths(ArrayList<File> files, ArrayList<String> dimensionNames) throws IOException {
        ArrayList<String> paths = new ArrayList<String>();
        for (File f : files) {
            String path = GetPathName(f, dimensionNames);
            int y;
            for (y = 0; y < paths.size(); y++) {
                if (paths.get(y).equals(path))
                    break;
            }
            if (y == paths.size())//path isn't already in paths
                paths.add(path);
        }
        return paths;
    }

    public static String GetPathName(File file, ArrayList<String> dimensionNames) throws IOException {
        String path = file.getPath();

        if (dimensionNames == null) {
            return path.substring(path.indexOf(file.getName()));
        }

        for (String dimension : dimensionNames) {
            if (path.contains(dimension)) {
                return path.substring(path.indexOf(dimension));
            }
        }

        return null;
    }
}
