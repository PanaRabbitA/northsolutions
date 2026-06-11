package org.example.proyecto_baseee.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceManager {

    private final String BASE_PATH = "C:\\Users\\leone\\OneDrive\\Escritorio\\Proyecto integrador norma\\Proyecto integrador";

    private final Map<String, Integer> servicePorts = Map.of(
            "ms-warehouse", 8084,
            "ms-sales", 8085,
            "ms-hr", 8086
    );

    public String stopService(String serviceName) {
        if (!servicePorts.containsKey(serviceName)) return "Servicio no gestionable";
        
        int port = servicePorts.get(serviceName);
        try {
            // Find PID using the port
            String cmd = String.format("powershell -Command \"Get-NetTCPConnection -LocalPort %d -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess\"", port);
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String pid = reader.readLine();
            
            if (pid != null && !pid.trim().isEmpty()) {
                String killCmd = "powershell -Command \"Stop-Process -Id " + pid.trim() + " -Force\"";
                Runtime.getRuntime().exec(killCmd);
                return "Servicio detenido exitosamente";
            } else {
                return "El servicio no estaba en ejecución";
            }
        } catch (Exception e) {
            return "Error al detener el servicio: " + e.getMessage();
        }
    }

    public String startService(String serviceName) {
        if (!servicePorts.containsKey(serviceName)) return "Servicio no gestionable";

        try {
            File dir = new File(BASE_PATH + "\\" + serviceName);
            // Start process hidden
            String[] cmd = {
                    "powershell",
                    "-Command",
                    "Start-Process -FilePath 'cmd.exe' -ArgumentList '/c .\\mvnw.cmd spring-boot:run' -WorkingDirectory '" + dir.getAbsolutePath() + "' -WindowStyle Hidden"
            };
            Runtime.getRuntime().exec(cmd);
            return "Comando de inicio enviado";
        } catch (Exception e) {
            return "Error al iniciar el servicio: " + e.getMessage();
        }
    }

    public boolean isServiceRunning(String serviceName) {
        if (!servicePorts.containsKey(serviceName)) return false;
        int port = servicePorts.get(serviceName);
        try {
            String cmd = String.format("powershell -Command \"Get-NetTCPConnection -LocalPort %d -ErrorAction SilentlyContinue\"", port);
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
