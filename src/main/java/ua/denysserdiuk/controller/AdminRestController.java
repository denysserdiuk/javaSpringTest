package denysserdiuk.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import denysserdiuk.model.Budget;
import denysserdiuk.model.Shares;
import denysserdiuk.model.Users;
import denysserdiuk.repository.BudgetRepository;
import denysserdiuk.repository.SharesRepository;
import denysserdiuk.repository.UserRepository;
import denysserdiuk.service.BudgetLinesService;
import denysserdiuk.service.SharesService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminRestController {

    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final SharesRepository sharesRepository;
    private final BudgetLinesService budgetLinesService;
    private final SharesService sharesService;

    public AdminRestController(UserRepository userRepository, BudgetRepository budgetRepository, SharesRepository sharesRepository, BudgetLinesService budgetLinesService, SharesService sharesService) {
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.sharesRepository = sharesRepository;
        this.budgetLinesService = budgetLinesService;
        this.sharesService = sharesService;
    }

    @PostMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestParam("username") String username) {
        Optional<Users> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            List<Budget> budgetLines = budgetRepository.findByUserId(user.getId());
            for (Budget budget: budgetLines) {
                budgetLinesService.deleteBudgetLine(budget);
            }
            List<Shares> shares = sharesRepository.findByUserId(user.getId());
            for (Shares share: shares){
                sharesService.deleteShare(share);
            }
            // Delete the user
            userRepository.delete(user);
            return new ResponseEntity<>("User and associated data deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }
}

