package com.safetynet.AppSafetyNet.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Implémentation de DataStorage utilisant Jackson pour lire/écrire dans un fichier JSON.
 * Cette classe agit comme une base de données en mémoire persisté par un fichier.
 */
@Slf4j
@Service
public class JsonDataStorageImpl implements InitializingBean, DataStorage {

    private final ObjectMapper mapper;
    private DataWrapper dataWrapper;
    private static final String FILEPATH = "data/data.json";

    /**
     * Appelé automatiquement après l'injection des dépendances par Spring.
     * Initialise le fichier de données puis les charge en mémoire.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        initializeDataFile();
        loadData();
    }

    public JsonDataStorageImpl(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void initializeDataFile() throws IOException {
        log.info("Initializing data file");
        File dataFile = new File(FILEPATH);
        InputStream dataResource = getClass().getClassLoader().getResourceAsStream("data.json");
        Assert.notNull(dataResource, "data.json file not found");
        Files.copy(dataResource, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void loadData() throws IOException {
            File dataFile = new File(FILEPATH);
            dataWrapper = mapper.readValue(dataFile, DataWrapper.class);
            log.info("Loading data from file :  {}", dataFile.getAbsolutePath());
            log.debug("Raw datas : {} ", dataWrapper);
    }

    @Override
    @SneakyThrows
    public void saveData(){
            File dataFile = new File(FILEPATH);
            mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, dataWrapper);
            log.info("Saving data to file :  {}", dataFile.getAbsolutePath());
    }

    @Override
    public List<Person> getPersons() {
        return dataWrapper.getPersons();
    }


    @Override
    public List<FireStation>  getFireStations() {
        return dataWrapper.getFirestations();
    }

    @Override
    public List<MedicalRecord> getMedicalRecords() {
        return dataWrapper.getMedicalrecords();
    }

}
