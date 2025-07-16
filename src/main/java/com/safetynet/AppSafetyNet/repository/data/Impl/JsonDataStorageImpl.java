package com.safetynet.AppSafetyNet.repository.data.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.AppSafetyNet.model.FireStation;
import com.safetynet.AppSafetyNet.model.MedicalRecord;
import com.safetynet.AppSafetyNet.model.Person;
import com.safetynet.AppSafetyNet.repository.data.DataStorage;
import com.safetynet.AppSafetyNet.repository.data.DataWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${application.file-path-to-persiste-data}")
    private String persistedDataFile;

    @Value("${application.base-data}")
    private String baseData;

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
        File dataFile = new File(persistedDataFile);
        InputStream dataResource = getClass().getClassLoader().getResourceAsStream(baseData);

        Assert.notNull(dataResource, baseData + " file not found");
        Files.copy(dataResource, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        log.info("Data file initialized at {}", dataFile.getAbsolutePath());
    }

    @Override
    public void loadData() throws IOException {
            File dataFile = new File(persistedDataFile);
            dataWrapper = mapper.readValue(dataFile, DataWrapper.class);
            log.debug("Raw datas loaded : {} ", dataWrapper);
            log.info("Loading data from file :  {}", dataFile.getAbsolutePath());
    }

    @Override
    public void saveData() {
        try {
            File dataFile = new File(persistedDataFile);
            mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, dataWrapper);
            log.info("Saving data to file :  {}", dataFile.getAbsolutePath());
            log.debug("Raw datas saved : {} ", dataWrapper);
        } catch (IOException e) {
            log.error("Failed to save data to file {}: {}", persistedDataFile, e.getMessage(), e);
            throw new IllegalStateException("Failed to save datafile in data/data.json " + persistedDataFile, e);
        }
    }

    @Override
    public List<Person> getPersons() {
        log.debug("Récupération de la liste des personnes ({} entrées)", dataWrapper.getPersons().size());
        return dataWrapper.getPersons();
    }


    @Override
    public List<FireStation>  getFireStations() {
        log.debug("Récupération de la liste des casernes ({} entrées)", dataWrapper.getFirestations().size());
        return dataWrapper.getFirestations();
    }

    @Override
    public List<MedicalRecord> getMedicalRecords() {
        log.debug("Récupération de la liste des dossiers médicaux ({} entrées)", dataWrapper.getMedicalrecords().size());
        return dataWrapper.getMedicalrecords();
    }

}
