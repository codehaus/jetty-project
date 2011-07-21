#Using the archetype

##Create your project
Go to a directory where you'd like to create your project.

Generate from the archetype:
    $ mvn archetype:generate -DarchetypeGroupId=org.mortbay.jetty.archetype -DarchetypeArtifactId=jetty-archetype-embedded

Follow the prompts and choose your values for groupId, artifactId, version, and base package

Your project will be created in a directory named for your artifact id. 

##Running Locally

Go to your project directory.

Build the application:

    $ mvn install

Set the location of your maven repository (usually in your home directory under .m2/repository)  into the REPO variable:

    $ export REPO="/home/user/.m2/repository"

Run the application:

    $ sh target/bin/webapp

Now you can test your application by opening a browser and going to http://localhost:8080
