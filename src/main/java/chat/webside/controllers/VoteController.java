package chat.webside.controllers;

import chat.dbside.models.User;
import chat.webside.controllers.util.ControllerUtil;
import chat.webside.controllers.util.SocialControllerUtil;
import chat.webside.controllers.util.VoteUtil;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VoteController {

    @Autowired
    private SocialControllerUtil util;

    @Autowired
    private VoteUtil voteUtil;

    @RequestMapping(value = "message/voteFor", method = GET, produces = "text/plain;charset=UTF-8")
    public String voteForMessage(HttpServletRequest request, Principal currentUser, Model model,
            @RequestParam("id") int commId, RedirectAttributes redirectAttributes) {
        User user = util.getSignedUpUser(request, currentUser);
        voteUtil.setModelForCommentVote(request, user, model, commId, true);
        return ControllerUtil.getPreviousPageByRequest(request).orElse("/");
    }

    @RequestMapping(value = "message/voteAgainst", method = GET, produces = "text/plain;charset=UTF-8")
    public String voteAgainstMessage(HttpServletRequest request, Principal currentUser, Model model,
            @RequestParam("id") int commId, RedirectAttributes redirectAttributes) {
        User user = util.getSignedUpUser(request, currentUser);
        voteUtil.setModelForCommentVote(request, user, model, commId, false);
        return ControllerUtil.getPreviousPageByRequest(request).orElse("/");
    }
}
