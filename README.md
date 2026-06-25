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

## cors 설정 필요 할 거 같어
https://control-front-navy.vercel.app/