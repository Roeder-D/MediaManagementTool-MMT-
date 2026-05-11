#!/bin/bash

# prepararion
echo "Updating system"
sudo apt update
sudo apt upgrade -y

# installing Docker
echo "Initializing Docker"

sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y

sudo systemctl start docker
sudo systemctl enable docker

# creating file-structure
echo "Creating file-path"
mkdir -p ~/mmt/DockerContainer/{mysql,mysqlconfig,htdocs,htdocs/mmt-cover_images,apache,secrets}

# creating configs
echo "creating configs"

echo "MySQL - creating config"
cat <<EOF > ~/mmt/DockerContainer/mysqlconfig/my.cnf
[mysqld]
# Disabeling stopwords for title search
innodb_ft_enable_stopword = OFF

# Reducing standard-parser token min size
innodb_ft_min_token_size = 1

# Reducing min token size for NGRAM-parser
ngram_token_size = 1
EOF

echo "Apache - creating config"
cat <<EOF > ~/mmt/DockerContainer/apache/apache.conf
<VirtualHost *:80>
    DocumentRoot /var/www/html

    <Directory /var/www/html>
        Options Indexes FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>

    ErrorLog \${APACHE_LOG_DIR}/error.log
    CustomLog \${APACHE_LOG_DIR}/access.log combined
</VirtualHost>
<Directory /var/www/html/mmt-cover_images>
    # Disable PHP engine in this specific folder
    php_admin_flag engine off
    # Prevent execution of any scripts
    Options -ExecCGI -Indexes
    AllowOverride None
</Directory>
EOF

echo "Apache creating Dockerfile"
cat <<EOF > ~/mmt/DockerContainer/apache/Dockerfile
FROM php:8.2-apache

# Installing PDO MySQL-Expansion
RUN docker-php-ext-install pdo pdo_mysql

# Activating of Apache Modules
RUN a2enmod rewrite

# Copying userspecified Apache-Configuration
COPY apache.conf /etc/apache2/sites-available/000-default.conf
EOF

# creating Docker Secret
echo "Creating Docker Secrets"

read -sp "Enter MySQL ROOT password: " ROOT_PASS
echo ""
read -sp "Enter MySQL username: " DB_USER
echo ""
read -sp "Enter MySQL USER password: " DB_PASS
echo ""

echo "$ROOT_PASS" > ~/mmt/DockerContainer/secrets/mysql_root_password.txt
echo "$DB_USER" > ~/mmt/DockerContainer/secrets/mysql_user.txt
echo "$DB_PASS" > ~/mmt/DockerContainer/secrets/mysql_password.txt

unset ROOT_PASS
unset DB_USER
unset DB_PASS

# setting up privileges for Secrets
chmod 600 ~/mmt/DockerContainer/secrets/*.txt

# creating Docker Compose file
echo "Creating Docker Compose"

cat <<EOF > ~/mmt/DockerContainer/docker-compose.yaml

name: mmt_project

services:
  mmt_mysql:
    image: mysql:latest
    container_name: mmt_mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD_FILE: /run/secrets/mysql_root_password
      MYSQL_USER_FILE: /run/secrets/mysql_user
      MYSQL_PASSWORD_FILE: /run/secrets/mysql_password
    ports:
      - "3325:3306"
    volumes:
      - ./mysql:/var/lib/mysql
      - ./mysqlconfig/my.cnf:/etc/mysql/conf.d/custom.cnf
    secrets:
      - mysql_root_password
      - mysql_user
      - mysql_password
    networks:
      - DockerNetwork

  mmt_phpmyadmin:
    image: phpmyadmin
    container_name: mmt_phpmyadmin
    restart: always
    environment:
      PMA_HOST: mmt_mysql
      PMA_PORT: 3306
    ports:
      - "8082:80"
    depends_on:
      - mmt_mysql
    networks:
      - DockerNetwork

  mmt_web:
    image: mmt_apache_image
    build:
      context: ./apache
      dockerfile: Dockerfile
    container_name: mmt_apache
    restart: always
    ports:
      - "8083:80"
    volumes:
      - ./htdocs:/var/www/html
      - ./apache/apache.conf:/etc/apache2/sites-available/000-default.conf
    networks:
      - DockerNetwork

secrets:
  mysql_root_password:
    file: ./secrets/mysql_root_password.txt
  mysql_user:
    file: ./secrets/mysql_user.txt
  mysql_password:
    file: ./secrets/mysql_password.txt

networks:
  DockerNetwork:
    driver: bridge
EOF

# starting Docker Container
echo "starting Container"
cd ~/mmt/DockerContainer
sudo docker compose up -d

# setting privileges
sudo usermod -aG docker $USER
sudo usermod -aG www-data $USER

sudo chown -R $USER:www-data ~/mmt/DockerContainer/htdocs
sudo chmod -R 775 ~/mmt/DockerContainer/htdocs

find ~/mmt/DockerContainer/htdocs -type d -exec chmod 775 {} \;
find ~/mmt/DockerContainer/htdocs -type f -exec chmod 664 {} \;

echo "restricting permissions vor cover images directory"
find ~/mmt/DockerContainer/htdocs/mmt-cover_images -type f -exec chmod 660 {} \;

echo "Setting ACLs"
sudo apt install acl -y
sudo setfacl -d -m g:www-data:rwX ~/mmt/DockerContainer/htdocs/mmt-cover_images
sudo setfacl -m g:www-data:rw ~/mmt/DockerContainer/htdocs/mmt-cover_images

echo "Setup completed!"
echo "Please logout and login again to apply the group-settings!"
