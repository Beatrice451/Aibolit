# Деплой SMTP-сервера

SMTP-сервер лежит в docker-compose вместе с остальным проектом.  
После `docker compose up -d --build` нужно ручками завершить настройку сервера.

В примерах команд подразумевается, что они выполняются _внутри_ контейнера, а хост работает на Linux.  
Если команды выполняются снаружи контейнера, следует использовать `docker compose exec mailserver <команда>`.

## 1. Правки DNS-записей

| Тип записи    | Имя      | Значение                                           |
|---------------|----------|----------------------------------------------------|
| `A`           | `mail`   | public IP сервера                                  |
| `MX`          | `@`      | `mail.domain.com` (приоритет 10)                   |
| `TXT` (SPF)   | `@`      | `v=spf1 mx ip4:ваш.публичный.IP -all`              |
| `TXT` (DMARC) | `_dmarc` | `v=DMARC1; p=none; rua=mailto:dmarc@chebupitsa.ru` |

> Важно! Система управления доменами может сама добавить свою SPF.  
> SPF-запись в DNS должна быть **_только одна_**. Т.е. запись регистратора нужно удалить.
__________

## 2. PTR (Reverse DNS)

Отвечает за резолвинг DNS, но наоборот: `IP-адрес -> доменное имя`.
Настраивается в панели провайдера хоста (не в DNS-зоне!)
________

## 3. DKIM

1. В контейнере инициализировать DKIM:
    ```shell
    setup config dkim
    ```
   Вывод будет содержать TXT-запись вида:
   > mail._domainkey.yourdomain.com. IN TXT "v=DKIM1; k=rsa; p=MIIBIjANBgkqhkiG9..."
2. Добавить её в DNS как `TXT`-запись с именем `mail._domainkey`.  
   (Например, `mail._domainkey.example.com`)

3. Дождаться обновления записи. Проверить можно командой:
    ```shell
    dig TXT mail._domainkey.example.com +short
    ```

___

## 4. Создание пользователей и алиасов

```shell

# Создание ящика
setup email add admin@yourdomain.com StrongPass123!

# Создание алиаса (пересылка)
# В данном примере письма, отправленные на info@example.com 
# будут перенаправляться на admin@example.com
setup alias add info@example.com admin@example.com

# Проверить список
setup email list
```

___

## 5. TLS

### Для локального запуска/тестов

Контейнер сам создаст `self-signed`. В конфигурации Spring нужно указать:

```properties
# .properties
spring.mail.properties.mail.smtp.ssl.trust=*
```

или:

```yaml
# .yaml
spring:
  mail:
    properties:
      mail:
        smtp:
          ssl:
            trust: *
```

### Для прода

#### Получение сертификата через `Certbot` (на хосте, не в контейнере)
```shell
# Установка (Ubuntu/Debian)
sudo apt update && sudo apt install certbot

# Запрос в ручном DNS-режиме
sudo certbot certonly --manual --preferred-challenges dns \
  -d smtp.example.com \
  -m admin@yourdomain.com \
  --agree-tos
```
`Certbot` попросит добавить в DNS запись:
> _acme-challenge.smtp.yourdomain.com TXT  "aBcDeFgHiJkLmNoPqRsTuVwXyZ..."

Добавляем её в панели хостера, ждём обновления записи, нажимаем Enter.  
Если всё верно - сертификат появится в `/etc/letsencrypt/live/smtp.example.com/`  
#### Подготовка файлов  

Контейнер ждёт строго определённые имена в `/etc/dms/certs/`:
```shell

sudo mkdir -p /opt/mailserver/certs
sudo cp /etc/letsencrypt/live/smtp.yourdomain.com/fullchain.pem /opt/mailserver/certs/mailserver.crt
sudo cp /etc/letsencrypt/live/smtp.yourdomain.com/privkey.pem /opt/mailserver/certs/mailserver.key
sudo chmod 644 /opt/mailserver/certs/mailserver.crt
sudo chmod 600 /opt/mailserver/certs/mailserver.key
```

> `fullchain.pem` = наш сертификат + промежуточный CA (обязательно для `TLS_LEVEL=intermediate`)
#### Подключение в docker-compose.yml (скорее всего я это уже добавил, но на всякий дублирую)
```yaml
services:
  mailserver:
    image: docker.io/mailserver/docker-mailserver:latest
    environment:
      - ENABLE_TLS=yes
      - TLS_LEVEL=intermediate
    volumes:
      - /opt/mailserver/certs:/etc/dms/certs:ro
      # остальные volumes
```

#### Переход с тестового `self-signed` на нормальный TLS
Актуально, если кое-кто забыл поправить конфиги бэкэнда.  
После перехода на валидный сертификат нужно проверить конфиг:
```yaml
spring:
  mail:
    host: smtp.example.com # ДОБАВИТЬ/УДОСТОВЕРИТЬСЯ В ПРИСУТСТВИИ
    port: 587 # ДОБАВИТЬ/УДОСТОВЕРИТЬСЯ В ПРИСУТСТВИИ
    properties:
      mail:
        smtp:
          auth: true # ДОБАВИТЬ/УДОСТОВЕРИТЬСЯ В ПРИСУТСТВИИ
          starttls:
            enable: true # ДОБАВИТЬ/УДОСТОВЕРИТЬСЯ В ПРИСУТСТВИИ
            required: true # ДОБАВИТЬ/УДОСТОВЕРИТЬСЯ В ПРИСУТСТВИИ
          ssl:
            trust: * # УБРАТЬ/УДОСТОВЕРИТЬСЯ В ОТСУТСТВИИ
```

