package chat.webside.controllers;

import chat.dbside.models.Chat;
import chat.dbside.models.User;
import chat.dbside.services.ini.MediaService;
import chat.webside.authorization.UserActionsProvider;
import chat.webside.controllers.util.ChatUtil;
import chat.webside.controllers.util.ControllerUtil;
import chat.webside.controllers.util.SocialControllerUtil;
import chat.webside.model.UserProfile;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ChatController {

    @Autowired
    @Qualifier("EAVStorageServiceUser")
    private MediaService serviceEntity;

    @Autowired
    private UserActionsProvider actionsProvider;

    @Autowired
    private SocialControllerUtil util;

    @Autowired
    private ChatUtil chatUtil;

    @RequestMapping(value = "chat/information", method = GET, produces = "text/plain;charset=UTF-8")
    public String show(HttpServletRequest request, Principal currentUser, Model model, @RequestParam("id") int id) throws Exception {
        util.setModel(request, currentUser, model);
        Chat chat = (Chat) serviceEntity.findById(id, Chat.class);
        User user = util.getSignedUpUser(request, currentUser);
        if (chat.getMembers().contains(user)
                || chat.getCreator().equals(user)) {
            chatUtil.setModelForChatShow(id, request, user, model);
        } else {
            throw new Exception("Unauthorized enter attempt!");
        }
        return "chatShow";
    }

    @RequestMapping(value = "chat/update", method = GET, produces = "text/plain;charset=UTF-8")
    public String updateChal(HttpServletRequest request, Principal currentUser, Model model, @RequestParam("id") int id) {
        util.setModel(request, currentUser, model);
        User user = util.getSignedUpUser(request, currentUser);
        chatUtil.setModelForChatShow(id, request, user, model);
        Chat chatToUpdate = (Chat) serviceEntity.findById(id, Chat.class);
        try {
            actionsProvider.canUpdateChallenge(user, chatToUpdate);
            return "chatNewOrUpdate";
        } catch (AccessDeniedException ex) {
            model.addAttribute("timestamp", DateUtils.addHours(new Date(), 3));
            model.addAttribute("status", 403);
            model.addAttribute("error", "Access is denied");
            model.addAttribute("message", ex.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = "chat/new", produces = "text/plain;charset=UTF-8")
    public String newChat(HttpServletRequest request, Principal currentUser, Model model) {
        util.setModel(request, currentUser, model);
        User user = util.getSignedUpUser(request, currentUser);
        chatUtil.setModelForNewChat(request, user, model);
        return "chatNewOrUpdate";
    }

    @RequestMapping(value = "chat/information", method = POST, produces = "text/plain;charset=UTF-8")
    public String saveOrUpdateChallenge(HttpServletRequest request, Principal currentUser, Model model,
            @Valid @ModelAttribute("chat") Chat chat, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, @RequestParam("image") String img,
            @RequestParam(required = false, value = "image-name") String imgName,
            @RequestParam("id-checked") List<Integer> selectedFriendsIds) {
        UserProfile userProfile = util.getUserProfile(request.getSession(), currentUser == null ? null : currentUser.getName());
        User user = util.getSignedUpUser(request, currentUser);
        //    boolean ignore = false;
//    	if(bindingResult.getAllErrors().size() == 1) {
//    		for(String s : bindingResult.getAllErrors().get(0).getCodes()) {
//        		if(s.contains("typeMismatch.challenge.tags")) {
//       			ignore = true;
//        			break;
//        		}
//        		else if(s.contains("typeMismatch.tags")) {
//        			ignore = true;
//        			break;
//        		}
//        		else if(s.contains("typeMismatch.java.util.List")) {
//        			ignore = true;
//        			break;
//        		}
//        	}
//    	}

        if (bindingResult.hasFieldErrors()) {
            util.setModel(request, currentUser, model);
            chatUtil.setModelForBadDateNewChal(chat, request, currentUser, model, img, imgName);
            model.addAttribute(bindingResult.getAllErrors());
            return "chatNewOrUpdate";
        } else {
            util.setModel(request, currentUser, model);
            chatUtil.setModelForNewOrUpdatedChalShow(chat, request, user, userProfile, model, img, selectedFriendsIds);
            redirectAttributes.addAttribute("id", chat.getId());
            return "redirect:information";
        }
    }

    @RequestMapping(value = "/acceptDefinition", method = GET, produces = "text/plain;charset=UTF-8")
    public String acceptChallengeDefinition(HttpServletRequest request, Principal currentUser, Model model,
            @RequestParam("id") int chalId) {
        util.setModel(request, currentUser, model);
        User user = (User) serviceEntity.findById(util.getUserProfile(request.getSession(), currentUser == null ? null : currentUser.getName()).getUserEntityId(), User.class);
        //  challengeDefUtil.setModelForAcceptChallengeDefinition(request, user, model, chalId);
        return ControllerUtil.getPreviousPageByRequest(request).orElse("/");
    }

    @RequestMapping(value = "/friendsForChallenge", method = GET)
    public String friendForChallenge(HttpServletRequest request, Principal currentUser,
            Model model,
            @RequestParam("id-checked") List<Integer> selectedFriendsIds,
            @RequestParam("chal-id") int chalId,
            @RequestParam("challenge-info") String chalInfo) {
        User currentDBUser = util.getSignedUpUser(request, currentUser);
        for (Integer id : selectedFriendsIds) {
            chatUtil.throwChat(id, currentDBUser, chalId, chalInfo);
        }
        return ControllerUtil.getPreviousPageByRequest(request).orElse("main");
    }

    @RequestMapping(value = "/challengesForFriend", method = GET)
    public String challengeForFriend(HttpServletRequest request, Principal currentUser,
            Model model,
            @RequestParam("id-checked") List<Integer> selectedChallengesIds,
            @RequestParam("user-id") int friendId,
            @RequestParam("challenge-info") List<String> messages) {

        User currentDBUser = util.getSignedUpUser(request, currentUser);
        for (int i = 0; i < selectedChallengesIds.size(); i++) {
            String message = messages.get(i).trim().isEmpty() ? null : messages.get(i);
            chatUtil.throwChat(friendId, currentDBUser, selectedChallengesIds.get(i), message);
        }
        return ControllerUtil.getPreviousPageByRequest(request).orElse("/");
    }

    @RequestMapping(value = "/acceptors", method = GET, produces = "text/plain;charset=UTF-8")
    public String selectAcceptors(HttpServletRequest request, Principal currentUser,
            Model model,
            @RequestParam("id") int challengeId) {
        util.setModel(request, currentUser, model);
        chatUtil.setModelForShowAcceptors(request, currentUser, model, challengeId);
        return "listSomething";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy HH:mm"), true);
        binder.registerCustomEditor(Date.class, editor);
    }
}
