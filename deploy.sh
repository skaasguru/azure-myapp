#!/bin/bash -x

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
    apt-get install -y default-jdk tomcat8 maven unzip
    echo JAVA_HOME=\"$(readlink -f /usr/bin/java | sed "s:/bin/java::")\" >> /etc/environment
    source /etc/environment
}

install_cassandra(){
    echo "deb http://www.apache.org/dist/cassandra/debian 39x main" | tee -a /etc/apt/sources.list.d/cassandra.sources.list
    curl https://www.apache.org/dist/cassandra/KEYS | apt-key add -
    apt-get update
    apt-get install -y cassandra

    systemctl start cassandra
    systemctl enable cassandra
}

pull_and_deploy_application(){
    git clone -b v4.0 https://github.com/skaasguru/azure-myapp.git
    cd azure-myapp
    mvn package
    cp ./target/webapp-1.0.0.war /var/lib/tomcat8/webapps/myapp.war
}

bootstrap_db(){
    while true; do
        cqlsh -e "DESCRIBE KEYSPACES;"
        if [[ $? == 0 ]]; then
            echo "Cassandra Ready... Creating DB..."
            cqlsh -f schema.cql
            break
        else
            echo "Cassandra is not Receiving connections... Trying again in 10 seconds"
            sleep 10
        fi
    done
}


create_swap
install_java_and_tomcat
install_cassandra
pull_and_deploy_application
bootstrap_db
