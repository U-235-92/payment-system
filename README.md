Запуск проекта:
- Находясь в корневой директории проекта [payment-system] использовать команду: docker-compose up
- В случае, если один из контейнеров с модулями [individuals-api] или [person-service] не запустился:
- - Использовать Docker desktop и в ручном режиме запустить контейнеры;
- - Использовать команду CLI для запуска контейнера Individuals-API (см. раздел "Запуск контейнеров на основе образов, созданных при использовании docker-compose up:");
- - Использовать команду CLI для запуска контейнера Person-service (см. раздел "Запуск контейнеров на основе образов, созданных при использовании docker-compose up:"); 

Тестирование модуля [individuals-api]:
- Перед тестированием ОБЯЗАТЕЛЬНО должен быть запущен Docker на машине пользователя;
- Перед запуском теста необходимо: 
[1]: Используя Gradle собрать jar-архив модуля [person-service]; 
[2]: Используя Docker собрать docker-образ модуля [person-service];

Сборка модуля [person-service] с использованием Gradle:
- Для Linux: находясь в корневой директории проекта [payment-system] gradlew :person-service:clean bootJar
- Для Windows: находясь в корневой директории проекта [payment-system] gradlew.bat :person-service:clean bootJar

Сборка модуля [individuals-api] с использованием Gradle:
- Для Linux: находясь в корневой директории проекта [payment-system] gradlew :individuals-api:clean bootJar
- Для Windows: находясь в корневой директории проекта [payment-system] gradlew.bat :individuals-api:clean bootJar

Сборка тестового docker-образа модуля [person-service] для запуска тестов модуля [individuals-api]:
- Находясь в директории модуля [person-service] использовать команду: docker build --tag payment-system/test-person-service:1.0.0 --file ./docker/dev/Dockerfile .

Запуск контейнеров на основе образов, созданных при использовании docker-compose up:
- Модуль [individuals-api]: docker run --network payment-system_app-network --network-alias individuals-api --name Individuals-API -p 8081:8081 -d u23592/individuals-api:latest
- Модуль [person-service]: docker run --network payment-system_app-network --network-alias person-service --name Person-service -p 8082:8082 -d u23592/person-service:latest