## rsync 명령어

```bash
rsync -avzP -e "ssh -p 1225 -i ~/.ssh/id_ed25519_server" control-api/ jin@heojineee.ddnsking.com:~/control-api/

rsync -avzP -e "ssh -p 1225 -i ~/.ssh/id_ed25519_server" control-api/ jin@heojineee.ddnsking.com:~/control-api/
```

## DB 명

- bus_service_db

## 기타 사항

- 9095 포트로 열기

## network 목록

    networks:
      - my-network
      - control-network
        - simul-network  

networks:
    my-network:
        external: true
    control-network:
        external: true

## backend container 관련

* **Image**: `control-api:latest`
* **Port Mapping**
    * Host: `9090`
    * Container: `9090/tcp`
    * IPv4: `0.0.0.0:9090 -> 9090/tcp`
    * IPv6: `[::]:9090 -> 9090/tcp`
* **Container Name**: `control-api`

- `http://control-api:9090`


- RestClient 사용해서 하는 게 좋아보임
  - 이거 말고 좋은 방법 있냐
- client 에서 생성 요청을 받으면 처리하는 식으로 하는 게 좋아보


