Astroboa Installation Guide on Linux
====================================

Before Installing Astroboa - Prerequisites
------------------------------------------

In order to install Astroboa, you need to have installed the Sun Java 6 JDK (not the OpenJDK), a running PostgreSQL server and also create a new user account named 'jboss'. The jboss user will eventually run Astroboa. You will also need a user account capable of running sudo or be the superuser on the server that will host Astroboa. In the rest of this guide we will run commands as root.

### Java Installation 

Download Java 6 JDK (Download [here](https://web.archive.org/web/20121111035706/http://www.oracle.com/technetwork/java/javase/downloads/index.html)). Currently, Astroboa has not been tested with OpenJDK, so we need the Java 6 JDK. We recommend to download jdk-6u24-linux-i586.bin file. Install Java 6 JDK running the command below as root. It will create a directory named jdk1.6.0\_24 in your current directory.
`sh jdk-6u24-linux-i586.bin`


Move the jdk1.6.0\_24 to /opt 
`mv jdk1.6.0_24 /opt`


Create the 'jboss' user


Create a 'jboss' user which will eventually run Astroboa, like this. 
`adduser jboss`


Now login as user 'jboss' using the password you entered when created the jboss user.


The next step is to set the _JAVA\_HOME_ environmental variable. Edit the _.bashrc_ file and append the following
`export` JAVA_HOME=/opt/jdk1.6.0_24 export PATH=$PATH:$JAVA_HOME/bin`


After saving re-initialize your environment running
`source ~/.bashrc`


To make sure that everything is ok, run 
`java -version`


You output should start like this 
`java version "1.6.0_24"`


### JBoss AS Installation 

Astroboa is deployed in the JBoss Application Server, so we need a fresh installation of the AS. 
We recommend not to use an existing JBoss installation to avoid strange incidents such as mismatches in various jar file dependencies. Go ahead and download the [JBOSS-AS-5.1.0.GA](http://www.jboss.org/jbossas/downloads.html) Application Server. 
Make sure you choose the jboss-5.1.0.GA-jdk6.zip file. Unzip it in some directory, you should have a jboss-5.1.0.GA directory. Then move the jboss-5.1.0.GA directory to /opt, like this:
`mv jboss-5.1.0.GA /opt`


Afterwards, change ownership to jboss user like this:
`chown -R jboss:jboss /opt/jboss-5.1.0.GA`


### PostgreSQL server installation

Astroboa needs a configured and running instance of a PostgreSQL server, version 8.2.x and later on. It has been tested with 8.2.x, 8.3.x, 8.4.x and 9.0.x versions. Recommended version is any of the 9.x.x versions.

#### PostgreSQL v9 Installation on CentOS

Here is how to install PostgreSQL v9 on CentOS. As root, run 

```bash
wget http://yum.pgrpms.org/reporpms/9.0/pgdg-centos-9.0-2.noarch.rpm

rpm -Uvh pgdg-centos-9.0-2.noarch.rpm

yum update

yum upgrade

yum groupinfo "PostgreSQL Database Server"

yum groupinstall "PostgreSQL Database Server"
```

Now, that PostreSQL v9 is installed, run the following to configure it
```bash
cat > /etc/sysconfig/pgsql/postgresql <<EOF PGDATA=/var/lib/pgsql/9.0/data \
PGPORT=5432 PGLOG=/var/lib/pgsql/9.0/pgstartup.log PGOPTS= EOF
```

Next, login as postgres user and edit .bash\_profile
`su - postgres $ vi .bash_profile

The .bash\_profile should contain the following:
```bash
[ -f /etc/profile ] && source /etc/profile 

PGDATA=/var/lib/pgsql/9.0/data

PATH=/usr/pgsql-9.0/bin:$PATH

export PGDATA PATH
```

Then, as user postgres, run 
`initdb --locale=C -E UNICODE -D /var/lib/pgsql/9.0/data`

Lastly, run as root
`chkconfig postgresql-9.0 on`

#### Configuring PostgreSQL

If you are running PostgreSQL version 8.4 and above you need to edit the postgresql.conf. This file may reside in /var/lib/pgsql/9.0/data if you have installed PostgreSQL version 9 in CentOS or in /etc/postgresql/8.3/main if you are running PostgreSQL version 8.4 on Ubuntu. So, edit postgresql.conf and change _max\_connections_ and _max\_prepared\_transactions_ to the settings below:
`max_connections = 100 max_prepared_transactions = 100`

You will need to restart your PostgreSQL server.

## Astroboa Installation
If all of the above are installed and prepared you can proceed to install Astroboa package. You need to be the root user to perform the installation so login as root before you proceed further.

If you are upgrading from BetaCMS to Astroboa, then all you have to do is to make sure that the correct paths to JBoss-4.2.3.GA and to BetaCMS repository home directory are set in the installation script. Astroboa installation will do the rest for you.

Upon downloading the package use the following command to unzip and untar the tgz archive:
`tar -zxvf astroboa-distribution-<version>.tgz`

The directory astroboa-distribution-<version> is created.

Go inside and locate _install.sh_ script.

This is a very basic installation script making several assumption on where JBOSS is installed, under which user JBOSS Application server is running and where the repository files will be installed.

If you do need to change one or more settings, open script with an editor and modify them according to your needs:

*   JBOSS\_DIR=/opt/jboss-5.1.0.GA
*   REPOSITORY\_DIR=/opt/Astroboa-Repositories
*   JBOSS\_USER=jboss
*   JBOSS\_DIR\_FOR\_BETACMS=/opt/jboss-4.2.3.GA   # Used only if an upgrade from BetaCMS takes place
*   REPOSITORY\_DIR\_FOR\_BETACMS=/opt/BetaCMS\_Repository # Used only if an upgrade from BetaCMS takes place   

Run the script with the following command (the command starts with a DOT):
`./install.sh`

The command does the following:

*   Copies third party libraries and modules in JBOSS lib and deploy directories.
*   Removes old Astroboa libs and modules in JBOSS lib and deploy directories.
*   Copies Astroboa libs and modules in JBOSS lib and deploy directories.
*   If no JDBC driver for PostgreSQL is found, you will be prompted to provide the version of installed PostgreSQL.
*   Removes some JBOSS libs and substitutes them with newer versions.
*   Creates (if not already there) the home directory (/opt/Astroboa-Repositories) where all necessary configuration and runtime files for repositories are kept. (If an upgrade from BetaCMS takes place, directory REPOSITORY\_DIR\_FOR\_BETACMS is renamed to REPOSITORY\_DIR).
*   Copies (if not already there) file 'astroboa-conf.xml' to _JBOSS\_DIR/server/default/conf_ directory. This file contains all necessary configuration settings for all available repositories. Please note that if Astroboa is installed for the first time, this file is empty and you MUST create a new repository prior to starting JBoss AS. (If an upgrade from BetaCMS takes place, then the previous configuration (file JBOSS\_DIR\_FOR\_BETACMS/server/default/conf/betacms-repositories-configuration.xml) is moved to  _JBOSS\_DIR/server/default/conf/astroboa-conf.xml_).
*   Copies Astroboa content management application (console_.war_) in JBOSS. This application is used to create, search, categorize, publish and share content and utilizes Astroboa repository infrastructure to do so.
*   Copies the Resource API web application (resource_\-api.war_) in JBOSS. This web application allows to access the repository functionality through http requests.

When the installation is finished, if Astroboa is installed for the first time, open _JBOSS\_DIR/run.conf_ and add the following to **JAVA\_OPTS** variable (replace previous values for -Xms and -Xmx that may exist):
`-Xms512m -Xmx512m -XX:MaxPermSize=128M -XX:PermSize=128M -Dfile.encoding=UTF-8`

These are the minimum recommended values.

**If Astroboa is installed for the first time follow the instructions [here](#add_new_repository) in order to create a repository, prior to starting JBoss.**

Start jboss (_JBOSS\_DIR/run.sh -b 0.0.0.0 &_) and go to the following links with your browser:

*   http://localhost:8080/console (this is the Astroboa content management application. To login use as username: SYSTEM and as password: betaconcept)

### Add a new repository to Astroboa
Astroboa supports multiple repositories all of which can be managed through the same web application (http://localhost:8080/console)

In order to configure a new repository, you need to execute the following script (located inside directory astroboa-distribution-<version>) :
`./create-new-repository.sh`

This script assumes the following settings

*   JBOSS\_DIR=/opt/jboss-5.1.0.GA
*   REPOSITORY\_DIR=/opt/Astroboa-Repositories
*   JBOSS\_USER=jboss

While script is executing you will be asked to provide values for the following questions

*   Provide repository identifier (Only Latin characters \[A-Z, a-z\] without spaces )  
    
    This is the most important variable of a repository configuration. It represents a unique identifier of the repository which  
    must contain only latin characters without spaces or digits.
    
    For the rest of the instructions, it is assumed that you have provided the value **REPOSITORY\_ID**
    
*   Provide a Greek label for repository
    
    Repository label in Greek (Locale = el). Default value is REPOSITORY\_ID
    
*   Provide an English label for repository
    
    Repository label in English (Locale = en). Default value is REPOSITORY\_ID
    
    As you may see in the resulting configuration XML file (_JBOSS\_DIR/server/default/conf/astroboa-conf.xml_), you may provide labels for more languages.
    
*   PostgreSQL username  
    
    During installation a database will be created and credentials must be provided. Default PostgreSQL user name is _postgres_
    
*   PostgreSQL password  
    
    During installation a database will be created and credentials must be provided. Default PostgreSQL user password is _postgres_
    
*   PostgreSQL Server Host or IP  
    
    During installation a database will be created and host or IP where PostgreSQL is installed must be provided. Default PostgreSQL host is _localhost_
    
*   PostgreSQL Server Port  
    
    During installation a database will be created and port where PostgreSQL is listening must be provided. Default PostgreSQL port is _5432_
    
*   PostgreSQL createdb script dir
    
    Directory path where PostgreSQL script _createdb_ is located 
    

Once you confirm provided values for the above settings, installation script will do the following:

*   creates a data source file needed to establish access to the repository database
    
    JBOSS\_DIR/server/default/deploy/astroboaDB-REPOSITORY\_ID-ds.xml
    
*   creates a data source file to establish access to JCR repository
    
    JBOSS\_DIR/server/default/deploy/jackrabbit-REPOSITORY\_ID-ds.xml
    
    **In case you want to rename the file make sure it is always greater in alphabetical order than the previous file. Otherwise an exception is thrown.**
*   creates repository home directory (_/opt/Astroboa-Repositories/REPOSITORY\_ID_) and further directory structure where all necessary configuration and runtime files for repository are kept.
    
    +- opt 
    | +- Astroboa-Repositories
      |  +- REPOSITORY\_ID
      |  \\- repository.xml 
      |  +- repository
         | +- astroboa\_schemata
         | \\- ..xsd (one or more)  
    
*   copies to directory _/opt/Astroboa-Repositories/REPOSITORY\_ID/repository/astroboa\_schemata_ default XSD schemas located in (astroboa_\-distribution-<version>/schemas-default_)
*   creates file _REPOSITORY\_DIR/REPOSITORY\_ID/repository.xml_
*   creates the following xml tag which is automatically appended to file _JBOSS\_DIR/server/default/conf/astroboa-conf.xml_  

```xml
<repository id="REPOSITORY_ID" jcr-repository-jndi-name="java:jcr/REPOSITORY_ID" 
    db-jndi-name="java:jdbc/myRepository"  
    serverAliasURL="[http://localhost:8080](http://localhost:8080)">
    
    <localization>
    
        <label xml:lang="el">Repository greek label</label>
    
        <label xml:lange="en">Repository english label</label>
    
    </localization>
    
<repository>
```

*   creates a database named after REPOSITORY\_ID using the following command
    
    ssh PostgreSQL\_USER@PostgreSQL\_HOST CREATEDB\_DIR/createdb -E UNICODE REPOSITORY\_ID
    

In case the above command is not successful then, log in as PostgreSQL\_USER user and execute the following command:
`createdb -E UNICODE REPOSITORY_ID`

Restart JBoss, visit http://localhost:8080/console and login to the newly created repository!
