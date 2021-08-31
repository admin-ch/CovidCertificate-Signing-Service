package ch.admin.bag.covidcertificate.signature.service;

import ch.admin.bag.covidcertificate.signature.config.ProfileRegistry;
import com.safenetinc.luna.LunaSlotManager;
import com.safenetinc.luna.exception.LunaCryptokiException;
import com.safenetinc.luna.provider.LunaProvider;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.security.Security;

@Component
@Slf4j
@Profile("!"+ ProfileRegistry.PROFILE_HSM_MOCK)
public class LunaSlotManagerWrapper {

    @Value("${crs.decryption.keyStoreSlotNumber}")
    private Integer keyStoreSlotNumber;

    @Value("${crs.decryption.keyStorePassword}")
    private String keyStorePassword;

    @PostConstruct
    private void init(){
        log.debug("slotManager Login with {}", keyStoreSlotNumber);
        login();
    }

    @PreDestroy
    public void logout(){
        LunaSlotManager.getInstance().logout();
    }

    public void login(){
        var lunaSlotManager = LunaSlotManager.getInstance();
        lunaSlotManager.enableReconnect();
        lunaSlotManager.login(keyStoreSlotNumber, keyStorePassword);
        log.info("Successfully logged in to Luna Client.");
    }

    @Synchronized
    public void reconnectHsmServer(){
        if(shouldRetryLogin()) {
            log.warn("Service not logged in to Luna Client. Retrying to login.");
            var lunaSlotManager = LunaSlotManager.getInstance();
            reRegisterLunaProvider();
            lunaSlotManager.reinitialize();
            lunaSlotManager.login(keyStoreSlotNumber, keyStorePassword);
            log.info("Successfully reconnected to Luna Client.");
        }
    }

    private void reRegisterLunaProvider(){
        var lunaProvider = new LunaProvider();
        Security.removeProvider(LunaProvider.getInstance().getName());
        Security.addProvider(lunaProvider);
    }

    public boolean isTokenPresent(){
       return LunaSlotManager.getInstance().isTokenPresent(keyStoreSlotNumber);
    }

    public boolean isLoggedIn(){
        return LunaSlotManager.getInstance().isLoggedIn();
    }

    public boolean shouldRetryLogin(){
        try {
            return !isTokenPresent()||!isLoggedIn();
        }catch (LunaCryptokiException e){
            log.debug("Could not retrieve whether Token is present or not", e);
            return true;
        }
    }
}
