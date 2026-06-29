## 배포 및 동기화 문서 (개인 참고용)

### 동기화 명령어

```bash
## public ip
rsync -avzP -e "ssh -p 1225 -i ~/.ssh/id_ed25519_server" simulator/ jin@heojineee.ddnsking.com:~/control-api/

## 내부 네트워크
rsync -avzP -e "ssh -p 22 -i ~/.ssh/id_ed25519_server" simulator/ jin@192.168.219.106:~/simulator/
```

### DB 명
- bus_service_db

### docker network 목록

```
   networks:
      - my-network
      - control-network
        - simul-network  

networks:
    my-network:
        external: true
    control-network:
        external: true
```
### backend container 관련

* **Image**: `control-api:latest`
* **Port Mapping**
    * Host: `9090`
    * Container: `9090/tcp`
    * IPv4: `0.0.0.0:9090 -> 9090/tcp`
    * IPv6: `[::]:9090 -> 9090/tcp`
* **Container Name**: `control-api`

- `http://control-api:9090`

### 실행 명령어

```shell
MYSQL_HOST=mysql-container \
MYSQL_PORT=3306 \
MYSQL_DATABASE=bus_service_db \
MYSQL_USERNAME=root \
MYSQL_PASSWORD=1234 \
docker compose up -d --build
```