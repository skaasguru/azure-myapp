sudo su
wget http://get.docker.com -O install_docker.sh
chmod +x install_docker.sh
./install_docker

git clone -b v5.0 https://github.com/skaasguru/azure-myapp.git

cd azure-app

ls -la

mkdir /cassandra
docker run --name cassandra -v /cassandra:/var/lib/cassandra -itd -p 9042:9042 cassandra

docker run -it --rm cassandra cqlsh IP_ADDRESS

cd AuthService
docker build -t authservice .
docker run -itd --name as -p 8080:8080 --env-file .env  authservice

cd ContactService
docker build -t contactservice .
docker run -itd --name as -p 8081:8080 --env-file .env  contactservice

cd GalleryService
docker build -t galleryservice .
docker run -itd --name as -p 8082:8080 --env-file .env  galleryservice

