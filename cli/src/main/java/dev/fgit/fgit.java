package dev.fgit;

import io.quarkus.runtime.QuarkusApplication;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.github.lalyos.jfiglet.FigletFont;
import static dev.fgit.banner.displayBanner;

public class fgit implements QuarkusApplication {
  @Override
  public int run(String... args) throws Exception {   
    
    /*
    //---Printing out fgit banner at start
    System.out.println(FigletFont.convertOneLine(
               "FGit "));
    //System.out.println(banner);
    */
    
    displayBanner();
    //--- If no argument provided ---
    if(args.length == 0){
        System.out.println("No input provided!!");
        help();
        return -1;
    }

    //Paths.get(".").toAbsolutePath().normalize().toString()
    Path directory = Paths.get(".").toAbsolutePath().normalize();
    System.out.println("Current working directory: "+directory.toString());

    //String command = args[0];
    if(args[0].equals("push"))
        try {
            String arguements  = String.join(" ",args);
            System.out.println(arguements);
            if(args.length > 1)
                arguements = arguements.substring(arguements.indexOf(" ")+1);
            else 
                arguements = "";
            push(directory,arguements);

            return 0; // Sucess Code
        } catch(Exception e) {
            e.printStackTrace();
            return 10;
        }
    else
        gitFallback(directory,args); // Fallback to default git command

    return 0; // all went good - no exit code should be set.

    
}//Main ends here



//Function to determine if the current OS is Windows
public static boolean isWindows(){
return  System.getProperty("os.name").toLowerCase().indexOf("windows")>=0;
}


// ====== fgit push: add,commit and push ======
public static void push(Path directory,String message)throws IOException, InterruptedException{    
     gitStage(directory); // git add -A

     if(message.length() == 0)
        message = "no commit message provided";
     
     gitCommit(directory,message ); // git commit -m"txt msg"
     
     gitPush(directory); //git push

}

public static void gitClone(Path directory, String originUrl) throws IOException, InterruptedException {
    //Function for git clonning
    runCommand(directory.getParent(), "git", "clone", originUrl, directory.getFileName().toString());
}

public static void gitStage(Path directory) throws IOException, InterruptedException {
    runCommand(directory, "git", "add", "-A");
}

public static void gitCommit(Path directory, String message) throws IOException, InterruptedException {
    runCommand(directory, "git", "commit", "-m", message);
}

public static void gitPush(Path directory) throws IOException, InterruptedException {
    runCommand(directory, "git", "push");
}

public static void gitFallback(Path directory,String... args) throws IOException, InterruptedException {
    
    String [] param = new String[args.length+1];
    
    param[0] = "git";
    
    for(int i=1;i<param.length;i++)
        param[i] = args[i-1];
    
    runCommand(directory, param);
}

public static String runCommand(Path directory, String... command) throws IOException, InterruptedException {
    //Function to Run Commands using Process Builder
    Process p=process_runner(directory,command);
    return gobbleStream(p);
}

public static Process process_runner(Path directory, String... command)throws IOException, InterruptedException{
    Objects.requireNonNull(directory, "directory");
    if (!Files.exists(directory)) {
        throw new RuntimeException("can't run command in non-existing directory '" + directory + "'");
    }
    ProcessBuilder pb = new ProcessBuilder()
            .command(command)
            .directory(directory.toFile());
    Process p = pb.start();

    int exit = p.waitFor();

    if (exit != 0) {
        throw new AssertionError(String.format("runCommand %s in %s returned %d", Arrays.toString(command), directory, exit));
    }
    return p;
}


public static String gobbleStream(Process p) throws IOException, InterruptedException{
    StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "E");
    StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "O");
    outputGobbler.start();
    errorGobbler.start();
    int exit = p.waitFor();
    if (exit != 0) {
        throw new AssertionError(String.format("runCommand returned %d", exit));
    }
    errorGobbler.join();
    outputGobbler.join();
    return outputGobbler.getExecResult()+errorGobbler.getExecResult();
}

//=============== FUNCTION TO DISPLAY HELP ===============
 public static void help(){
     System.out.println("\nFGit: Faster and Smarter git experience.\n" );
     System.out.println("usage: fgit <command> [<args>]\n");
     System.out.println("-h, --help     Display help/info\n");
     System.out.println("Commands:\n");
     System.out.println("===============================================");
     System.out.println("   push        Executes add ., commit and push" );
     System.out.println("   random      not defined currently");
     System.out.println();
 }


private static class StreamGobbler extends Thread {
    private  String exec_result;

    private final InputStream is;
    private final String type;

    private StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        exec_result=""; //Resets result variable for every new process execution 
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println(type + "> " + line);
                exec_result+=line;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            
        }
    }

    public String getExecResult(){
        return exec_result;
    }
}








}
