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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class fgit implements QuarkusApplication {
  @Override
  public int run(String... args) throws Exception {   
    
    
    /*for(String s:args)
    System.out.println("\nTarget : "+s);//Printing Received Arguements */
    if(args.length == 0){
        System.out.println("No input provided!!");
        help();
        return -1;
    }

    //String command = args[0];
    if(args[0].equals("push"))
    try {
        //Paths.get(".").toAbsolutePath().normalize().toString()
        Path directory = Paths.get(".").toAbsolutePath().normalize();
        String arguements  = String.join(" ",args);
        if(args.length > 1)
            arguements = arguements.substring(arguements.indexOf(" "+1));
        else 
            arguements = "";
        push(directory,arguements);

        /*String repo_name = url.substring(url.lastIndexOf("/"), url.lastIndexOf(".")); //Extracts the Name of Repository

        String originUrl = url;
        Path directory = Paths.get(clone_path + repo_name);

        if (!Files.exists(directory)) //Check if the user cloned the repo previously and in that case no cloning is needed
            gitClone(directory, originUrl);


        //Launching Editor on the Cloned Directory
        System.out.println("Launching  Editor Now...");
        launch_editor(directory.getParent(), ide, clone_path + repo_name);*/

        return 0; // Sucess Code
    } catch(Exception e) {
        e.printStackTrace();
        return 10;
    }
    return 0; // all went good - no exit code should be set.

    
}//Main ends here



//Function yo determine if the current OS is Windows
public static boolean isWindows(){
return  System.getProperty("os.name").toLowerCase().indexOf("windows")>=0;
}

/*
//Function to fetch config file
public static File getConfigFile(){
    String userHome = System.getProperty( "user.home" ); //Get User Home Directory: /home/user_name

    File configFile = new File(userHome + "/.config/fgit.yaml"); //Loading YAML

    return configFile;

}
*/

//================================================

//Function to edit configuration and serves for command line fgit config editor
/*
public static void editConfig()throws Exception{
    System.out.println("\n-------------------------------------------------------");
    System.out.println("\t\tfgit Configuration Editor");
    System.out.println("-------------------------------------------------------\n");
    String clone_path="";//Holds path to  destination Where the Repository Must Be Clonned
    String ide="";  //Holds command for IDE to open upon
   

    File configFile = getConfigFile();
    
    BufferedReader reader= new BufferedReader(new InputStreamReader(System.in));
    
    
    //Reading Configuration File
    try {
        if (!configFile.exists()) {
            //Incase no config file exists
            System.out.println("\n-No configurations exist for fgit...You'll have to  configure it");
            System.out.println("\n-New  configuration file will be created at: " + configFile.getAbsolutePath() + "\n");
            configFile.createNewFile();
            }
            
            
            //--------------------------First we'll input preferred IDE from user------------------------------

            int id=0;
            while(id!=1||id!=2||id!=3){
            System.out.println("\n--------Chose the preferred IDE --------\n 1.for vscode \n 2.for eclipse \n 3.for IntelliJ_IDEA ");
            
            id=Integer.parseInt(reader.readLine());
            
            if(id==1){ide=isWindows()?"code.cmd":"code";System.out.println("\n--------Selected IDE:VsCode--------");break;}
            else
            if(id==2){ide="eclipse";System.out.println("\n--------Selected IDE:Eclipse--------");break;}
            else
            if(id==3){ide="idea";System.out.println("\n--------Selected IDE:IntelliJ_IDEA--------");break;}
            else
            System.out.println("\n--------Invalid Input!! Try Again--------");
            }

            //-----------Now we'll get preferred clone path on local file system from user--------------
            while(true){
            System.out.println("\n--------Enter preferred Clonning path--------");
            clone_path=reader.readLine();
            //We'll check if the path enterd by user is a valid path or not
            File tmp=new File(clone_path);
            if(tmp.exists())break;
            //Incase of Invalid path he'll be shown an error and directed to try again
            System.out.println("\n--------Invalid Path!! Try Again--------");
            }
            //----------Now we'll write configurations to the YAML FILE------------
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Config configuration=new Config(ide,clone_path);
            mapper.writeValue(configFile, configuration); //Writing to YAML File
            
        
        
    }
    catch (Exception e) {
        e.printStackTrace();
     
    }

}
*/

// ====== 

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
