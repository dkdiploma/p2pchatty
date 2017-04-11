package chat.webside.controllers;

import org.springframework.stereotype.Controller;

@Controller
public class ChallengeInstanceController {

//    @Autowired
//    private SocialControllerUtil util;
//
//    @Autowired
//    @Qualifier("EAVStorageServiceUser")
//    private MediaService serviceEntity;
//    @Autowired
//    private ChallengeInstanceUtil challengeInsUtil;
//
//    @RequestMapping(value = "challengeins/information", method = GET, produces = "text/plain;charset=UTF-8")
//    public String showInstance(HttpServletRequest request, Principal currentUser, Model model, @RequestParam("id") int id) {
//        util.setModel(request, currentUser, model);
//        User user = util.getSignedUpUser(request, currentUser);
//        challengeInsUtil.setModelForChallengeInstanceShow(id, request, user, model);
//        return "chalShow";
//    }
//
//    @RequestMapping(value = "challengeins/newstep", method = POST, produces = "text/plain;charset=UTF-8")
//    public String newStep(HttpServletRequest request, Principal currentUser, Model model,
//            @Valid @ModelAttribute("step") ChallengeStep step, BindingResult bindingResult,
//            @RequestParam("id") int id, RedirectAttributes redirectAttributes,
//            @RequestParam("image") String img,
//            @RequestParam(required = false, value = "image-name") String imgName) {
//        ChallengeInstance challenge = (ChallengeInstance) serviceEntity.findById(id, ChallengeInstance.class);
//        if (bindingResult.hasFieldErrors()
//                || challenge.getDate().before(step.getDate())
//                || step.getDate().before(DateUtils.addHours(new Date(), 3))) {
//            util.setModel(request, currentUser, model);
//            User user = util.getSignedUpUser(request, currentUser);
//            challengeInsUtil.setModelForBadStepChal(id, step, request, user, model,img, imgName);
//            model.addAttribute(bindingResult.getAllErrors());
//            if (challenge.getDate().before(step.getDate())
//                    || step.getDate().before(DateUtils.addHours(new Date(), 3))) {
//                model.addAttribute("dateError", true);
//            } else {
//                model.addAttribute("dateError", false);
//            }
//            //#stepform
//            return "chalShow";
//        } else {
//            challengeInsUtil.setModelForNewStepForChallenge(request, currentUser, model, step, img, id);
//            redirectAttributes.addAttribute("id", id);
//            return "redirect:information";
//        }
//    }
//
//    @RequestMapping(value = "challengeins/subscribe", method = GET, produces = "text/plain;charset=UTF-8")
//    public String subscribeOnChallengeInstance(HttpServletRequest request, Principal currentUser, Model model,
//            @RequestParam("id") int chalId, RedirectAttributes redirectAttributes) {
//        User user = (User) serviceEntity.findById(util.getUserProfile(request.getSession(), currentUser == null ? null : currentUser.getName()).getUserEntityId(), User.class);
//        challengeInsUtil.setModelForInstanceSubscribe(request, user, model, chalId);
//        redirectAttributes.addAttribute("id", chalId);
//        return "redirect:information";
//    }
//
//    @RequestMapping(value = "challengeins/close", method = GET, produces = "text/plain;charset=UTF-8")
//    public String closeChallenge(HttpServletRequest request, Principal currentUser, Model model,
//            @RequestParam("id") int chalId, RedirectAttributes redirectAttributes) {
//        challengeInsUtil.setModelForCloseChallenge(request, currentUser, model, chalId);
//        redirectAttributes.addAttribute("id", chalId);
//        return "redirect:information";
//    }
//
//    @RequestMapping(value = "/accept", method = GET, produces = "text/plain;charset=UTF-8")
//    public String accept(HttpServletRequest request, Principal currentUser, Model model,
//            @RequestParam("id") int requestId) {
//        util.setModel(request, currentUser, model);
//        User user = util.getSignedUpUser(request, currentUser);
//        challengeInsUtil.setModelForAcceptOrDeclineChallenge(request, user, model, requestId, true);
//        return ControllerUtil.getPreviousPageByRequest(request).orElse("/");
//    }
//
//    @RequestMapping(value = "/decline", method = GET, produces = "text/plain;charset=UTF-8")
//    public String decline(HttpServletRequest request, Principal currentUser,
//            Model model,
//            @RequestParam("id") int requestId) {
//        util.setModel(request, currentUser, model);
//        User user = util.getSignedUpUser(request, currentUser);
//        challengeInsUtil.setModelForAcceptOrDeclineChallenge(request, user, model, requestId, false);
//        return ControllerUtil.getPreviousPageByRequest(request).orElse("/");
//    }
//
//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy HH:mm"), true);
//        binder.registerCustomEditor(Date.class, editor);
//    }
}
