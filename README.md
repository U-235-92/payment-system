
### ***Запуск проекта:***
- Находясь в корневой директории проекта [payment-system], использовать команду: **docker-compose up**
- В случае, если один из контейнеров с модулями [individuals-api] или [person-service] не запустился:
- - Использовать Docker desktop и в ручном режиме запустить контейнеры;
- - Использовать команду CLI для запуска контейнера Individuals-API (см. раздел *Запуск контейнеров на основе образов, созданных при использовании docker-compose up*);
- - Использовать команду CLI для запуска контейнера Person-service (см. раздел *Запуск контейнеров на основе образов, созданных при использовании docker-compose up*);

----------
 


### ***Сборка модуля [person-service] с использованием Gradle:***
- Для Linux. Находясь в корневой директории проекта [person-service], использовать команду: **gradlew clean bootJar**
- Для Windows. Находясь в корневой директории проекта [person-service], использовать команду: **gradlew.bat clean bootJar**

----------



### ***Сборка модуля [individuals-api] с использованием Gradle:***
- Для Linux. Находясь в корневой директории проекта [individuals-api], использовать команду: **gradlew clean bootJar**
- Для Windows. Находясь в корневой директории проекта [individuals-api], использовать команду: **gradlew.bat clean bootJar**

----------



### ***Запуск контейнеров на основе образов, созданных при использовании docker-compose up:***
- Находясь в корневой директории проекта [individuals-api], использовать команду: **docker run --env-file .env --network payment-system_app-network --network-alias individuals-api --name Individuals-API -p 8081:8081 -d u23592/individuals-api:1.0.0**
- Находясь в корневой директории проекта [person-service], использовать команду: **docker run --env-file .env --network payment-system_app-network --network-alias person-service --name Person-service -p 8082:8082 -d u23592/person-service:1.0.0**