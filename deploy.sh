#!/bin/bash -x

DB_SERVER_IP=''
STORAGE_ACCOUNT_NAME=''
STORAGE_ACCOUNT_KEY=''
STORAGE_ACCOUNT_SHARE=''

create_swap(){
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile   none    swap    sw    0   0' >> /etc/fstab
}

install_java_and_tomcat(){
    apt-get update
    apt install software-properties-common -y
    apt-get install -y default-jdk tomcat8 maven
    echo JAVA_HOME=\"$(readlink -f /usr/bin/java | sed "s:/bin/java::")\" >> /etc/environment
    source /etc/environment
}

pull_and_deploy_application(){
    git clone -b v1.0 https://github.com/skaasguru/azure-myapp.git
    cd azure-myapp
    mvn package
    cp ./target/webapp-1.0.0.war /var/lib/tomcat8/webapps/myapp.war
}

create_uploads_folder(){
    mkdir -p /data /etc/smbcredentials
    echo "username=$STORAGE_ACCOUNT_NAME" >> /etc/smbcredentials/fileshare.cred
    echo "password=$STORAGE_ACCOUNT_KEY" >> /etc/smbcredentials/fileshare.cred
    chmod 600 /etc/smbcredentials/fileshare.cred
    echo "//$STORAGE_ACCOUNT_NAME.file.core.windows.net/$STORAGE_ACCOUNT_SHARE /data cifs nofail,vers=3.0,credentials=/etc/smbcredentials/fileshare.cred,dir_mode=0777,file_mode=0777,serverino" >> /etc/fstab
    mount -a
}

configure_app(){
    echo -e '\nFILE_LOCATION="/data"\nDB_STRING="jdbc:mysql://'$DB_SERVER_IP':3306/myapp"' >> /etc/default/tomcat8
    service tomcat8 restart
}



create_swap
install_java_and_tomcat
pull_and_deploy_application
create_uploads_folder
configure_app
