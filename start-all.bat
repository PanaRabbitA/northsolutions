@echo off
echo ===================================================
echo Iniciando Sistema Central - North Solutions
echo ===================================================

echo [1/4] Iniciando Servidor de Nombres (Eureka)...
start "Enterprise Hub (Eureka - 8761)" cmd /k "cd enterprisehub && mvnw.cmd spring-boot:run"

:: Esperamos 15 segundos para que Eureka levante completamente antes de lanzar los demas
timeout /t 15

echo [2/4] Iniciando Servicios Base (Autenticacion y Usuarios)...
start "MS Auth (8081)" cmd /k "cd ms-auth && mvnw.cmd spring-boot:run"
start "MS User (8082)" cmd /k "cd ms-user && mvnw.cmd spring-boot:run"

:: Esperamos 5 segundos
timeout /t 5

echo [3/4] Iniciando Microservicios de Negocio...
start "MS Warehouse (8084)" cmd /k "cd ms-warehouse && mvnw.cmd spring-boot:run"
start "MS Sales (8085)" cmd /k "cd ms-sales && mvnw.cmd spring-boot:run"
start "MS HR (8086)" cmd /k "cd ms-hr && mvnw.cmd spring-boot:run"
start "Workflow Service (8087)" cmd /k "cd workflow-service && mvnw.cmd spring-boot:run"
start "MS Dashboard (8083)" cmd /k "cd ms-dashboard && mvnw.cmd spring-boot:run"

echo [4/4] Iniciando Aplicacion Web (Frontend)...
start "Frontend (Vue)" cmd /k "cd vue-microservicios-front && npm run dev"

echo ===================================================
echo Todos los servicios han sido lanzados.
echo Puedes cerrar cada ventana negra (cmd) de forma individual para apagar ese servicio.
echo ===================================================
pause
