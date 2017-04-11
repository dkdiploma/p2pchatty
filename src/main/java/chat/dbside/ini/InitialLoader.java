package chat.dbside.ini;

import java.util.logging.Logger;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import chat.common.AccessProp;
import chat.dbside.models.Chat;
import chat.dbside.models.Message;
import chat.dbside.models.Image;
import chat.dbside.models.Tag;
import chat.dbside.models.User;
import chat.dbside.property.PropertyDB;
import chat.dbside.services.ini.MediaService;
import chat.webside.imagesstorage.ImageStoreService;

import java.util.logging.Level;

@Component
public class InitialLoader {

    @Autowired
    @Qualifier("EAVStorageServiceUser")
    private MediaService serviceEntityInit;

    @Autowired
    @Qualifier("storageServiceProperty")
    private MediaService serviceProperty;

    public void initial() {
        boolean iniFlag = false;
        PropertyDB version = (PropertyDB) serviceProperty.findById("version", PropertyDB.class);

        if (version == null) {
            iniFlag = true;
            version = new PropertyDB("version", "0");
            init(1, 2, 2, 4, 7);
            serviceProperty.save(version);
        }
        Integer nVersion = Integer.valueOf(version.getValue());

        //init(5, 2,2, 4, 3);
        List<User> users = serviceEntityInit.getAll(User.class);
        User user1 = (User) serviceEntityInit.findById(users.get(0).getId(), User.class);

        List<Chat> challenges = serviceEntityInit.getAll(Chat.class);
        List<Message> comments = serviceEntityInit.getAll(Message.class);
    }

    private Integer createContext(Integer versionDB) throws Exception {
        Integer versionApp = Integer.valueOf(AccessProp.getProperties().getCurrentVersionDB());

        //ParserDBConfiguration p = new ParserDBConfiguration();
        /*
        if (versionDB > 0) {
            ContextType contextType = ContextType.getInstance();

            for (TypeOfAttribute t : (List<TypeOfAttribute>) serviceAttr.getAll(TypeOfAttribute.class)) {
                p.getAllAttribute().put(t.getName(), t);

                //p.getAddCandiateAttributes().put(t.getName(), t);
                //contextType.add(t);
            }

            for (TypeOfEntity t : (List<TypeOfEntity>) serviceEntity.getAll(TypeOfEntity.class)) {
                p.getAllEntities().put(t.getNameTypeEntity(), t);
                //p.getAddCandidateEntities().put(t.getNameTypeEntity(), t);
                //contextType.add(t);
            }
        }

        for (Integer i = versionDB + 1; i <= versionApp; i++) {
            String filePath = AccessProp.getProperties().getStructureDBPath()
                    + "v" + i.toString() + ".xml";

            InputStream input = new ClassPathResource(filePath).getInputStream();
            //p = new ParserDBConfiguration();    		
            p.applyConfiguration(input);

            for (TypeOfAttribute t : p.getAddCandiateAttributes()) {
                serviceAttr.save(t);
            }
            for (TypeOfAttribute t : p.getRmCandiateAttributes()) {
                serviceAttr.delete(t);
            }

            for (TypeOfEntity t : p.getAddCandidateEntities()) {
                serviceEntity.save(t);
            }
            for (TypeOfEntity t : p.getUpdateCandidateEntities()) {
                serviceEntity.update(t);
            }
            for (TypeOfEntity t : p.getRmCandidateEntities()) {
                serviceEntity.delete(t);
            }

        }

        for (TypeOfAttribute t : (List<TypeOfAttribute>) serviceAttr.getAll(TypeOfAttribute.class)) {
            ContextType.getInstance().add(t);
        }

        for (TypeOfEntity t : (List<TypeOfEntity>) serviceEntity.getAll(TypeOfEntity.class)) {
            ContextType.getInstance().add(t);
        }*/
        return versionApp;
    }

    private static String[] generateRandomWords(int numberOfWords) {
        String[] randomStrings = new String[numberOfWords];
        Random random = new Random();
        for (int i = 0; i < numberOfWords; i++) {
            char[] word = new char[random.nextInt(8) + 3]; // words of length 3 through 10. (1 and 2 letter words are boring.)
            for (int j = 0; j < word.length; j++) {
                word[j] = (char) ('a' + random.nextInt(26));
            }
            randomStrings[i] = new String(word);
        }
        return randomStrings;
    }

    private Message createNewEmbeddedComments(int countOfEmbedence, User userToCreate) {
        Message comment = createNewComment(userToCreate);
        if (countOfEmbedence > 0) {
            comment.addComment(createNewEmbeddedComments(countOfEmbedence - 1, userToCreate));
        }
        return comment;
    }

    private Message createNewComment(User userToCreate) {
        StringBuilder text = new StringBuilder();
        Message comment = new Message();
        for (String word : generateRandomWords(new Random().nextInt(20) + 2)) {
            text.append(word).append(" ");
        }
        text.append(".");
        comment.setText(text.toString());
        comment.setDate(DateUtils.addHours(new Date(), 3));
        comment.setAuthor(userToCreate);
        serviceEntityInit.save(comment);
        return comment;
    }

    public void createTags() {
        Tag tag1 = new Tag();
        tag1.setName("преодоление");
        serviceEntityInit.save(tag1);
        Tag tag2 = new Tag();
        tag2.setName("шутка");
        serviceEntityInit.save(tag2);
        Tag tag4 = new Tag();
        tag4.setName("смелость");
        serviceEntityInit.save(tag4);
        Tag tag5 = new Tag();
        tag5.setName("безумие");
        serviceEntityInit.save(tag5);
        Tag tag6 = new Tag();
        tag6.setName("борьба");
        serviceEntityInit.save(tag6);
    }

    public void init(int countOfUsers, int countOfChalDefs, int countOfInstanses, int countOfComments, int countOfEmbedence) {
        try {
            ImageStoreService.saveDefaultImage(new File("src/main/resources/static/images/photo_not_available.jpg"));
            ImageStoreService.saveDefaultUserImage(new File("src/main/resources/static/images/user_photo_not_available.jpg"));
        } catch (Exception ex) {
            Logger.getLogger(InitialLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> images = new ArrayList<>();
        images.add("src/main/resources/static/images/firstExampleChallenge.jpg");
        images.add("src/main/resources/static/images/secondExampleTask.png");
        images.add("src/main/resources/static/images/wheely.jpg");
        images.add("src/main/resources/static/images/speed.jpg");
        images.add("src/main/resources/static/images/break.png");
        images.add("src/main/resources/static/images/AvaDefault.jpg");
        createTags();
        for (int i = 0; i < countOfUsers; i++) {
            User userToCreate = new User();
            userToCreate.setRating(0);
            userToCreate.setName(generateRandomWords(1)[0] + "-user");
            serviceEntityInit.save(userToCreate);
            Image picForUser = new Image();
            picForUser.setIsMain(Boolean.TRUE);
            picForUser.setIsForComment(Boolean.FALSE);
            serviceEntityInit.save(picForUser);
            Image picMinForUser = new Image();
            picMinForUser.setIsForComment(Boolean.TRUE);
            picMinForUser.setIsMain(Boolean.FALSE);
            serviceEntityInit.save(picMinForUser);
            try {
                int rand = new Random().nextInt(images.size());
                ImageStoreService.saveMiniImage(new File(images.get(rand)), picMinForUser);
                serviceEntityInit.update(picMinForUser);
                ImageStoreService.saveImage(new File(images.get(rand)), picForUser);
                picForUser.setMinVersionId(picMinForUser.getId());
                serviceEntityInit.update(picForUser);
            } catch (Exception ex) {
                Logger.getLogger(InitialLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            userToCreate.addImage(picForUser);
            userToCreate.addImage(picMinForUser);
            serviceEntityInit.update(userToCreate);
            for (int k = 0; k < countOfChalDefs; k++) {
                Chat chalToCreate = new Chat();
             //   chalToCreate.setRating(0);
                chalToCreate.setName(generateRandomWords(1)[0] + "-challenge");
                StringBuilder text = new StringBuilder();
                for (String word : generateRandomWords(new Random().nextInt(20) + 2)) {
                    text.append(word).append(" ");
                }
                text.append(".");
                chalToCreate.setDescription(text.toString());
                chalToCreate.setDate(DateUtils.addMonths(new Date(), 1));

                chalToCreate.setCreator(userToCreate);
                serviceEntityInit.save(chalToCreate);
                for (int m = 0; m < countOfComments; m++) {
                    Message comment = createNewEmbeddedComments(countOfEmbedence, userToCreate);
                    chalToCreate.addMessage(comment);
                }
                serviceEntityInit.update(chalToCreate);
                Image pic = new Image();
                pic.setIsMain(Boolean.TRUE);
                serviceEntityInit.save(pic);
                try {
                    ImageStoreService.saveImage(new File(images.get(new Random().nextInt(images.size()))), pic);
                    serviceEntityInit.update(pic);

                } catch (Exception ex) {
                    Logger.getLogger(InitialLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
                chalToCreate.addImage(pic);
                serviceEntityInit.update(chalToCreate);
            }
        }
    }
}
