package denysserdiuk.controller;

import denysserdiuk.repository.SharesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import denysserdiuk.model.Budget;
import denysserdiuk.model.Shares;
import denysserdiuk.model.StockPrice;
import denysserdiuk.model.Users;
import denysserdiuk.repository.StockPriceRepository;
import denysserdiuk.repository.UserRepository;
import denysserdiuk.service.BudgetService;
import denysserdiuk.service.SharesService;
import denysserdiuk.service.StockPriceService;
import denysserdiuk.utils.SecurityUtils;

import java.util.List;
import java.util.Optional;

@Controller
public class SharesWebController {
    private final UserRepository userRepository;
    private final SharesService sharesService;
    private final StockPriceRepository stockPriceRepository;
    private final StockPriceService stockPriceService;
    private final BudgetService budgetService;
    private final SharesRepository sharesRepository;

    public SharesWebController(UserRepository userRepository, SharesService sharesService, StockPriceRepository stockPriceRepository, StockPriceService stockPriceService, BudgetService budgetService, SharesRepository sharesRepository) {
        this.userRepository = userRepository;
        this.sharesService = sharesService;
        this.stockPriceRepository = stockPriceRepository;
        this.stockPriceService = stockPriceService;
        this.budgetService = budgetService;
        this.sharesRepository = sharesRepository;
    }

    @PostMapping("/addShare")
    public String addShare(Shares share, Model model) {
        String username = SecurityUtils.getAuthenticatedUsername();

        Users user = userRepository.findByUsername(username);
        share.setUser(user);

        StockPrice existingStockPrice = stockPriceRepository.findByTicker(share.getTicker());
        if (existingStockPrice == null) {
            stockPriceService.updateStockPrice(share.getTicker());
            existingStockPrice = stockPriceRepository.findByTicker(share.getTicker());
        }

        double profit = sharesService.updateShareProfit(share, existingStockPrice);
        share.setProfit(profit);

        Optional <Shares> existingShare = Optional.ofNullable(sharesRepository.findByTickerAndUserId(share.getTicker(), user));
        if (existingShare.isPresent()) {
            Shares shareToUpdate = existingShare.get();
            double totalValue = (shareToUpdate.getAmount() * shareToUpdate.getPrice())
                    + (share.getAmount() * share.getPrice());
            shareToUpdate.setAmount(shareToUpdate.getAmount() + share.getAmount());
            shareToUpdate.setPrice((double) Math.round(totalValue / shareToUpdate.getAmount() * 100) /100);
            shareToUpdate.setProfit(sharesService.updateShareProfit(shareToUpdate, existingStockPrice));
            sharesRepository.save(shareToUpdate); // Save the updated share
            model.addAttribute("message", "Share amount updated");
            return "shares";
        }
        else {
            Budget budget = new Budget();
            budget.setUser(user);
            budget.setDescription(share.getTicker());
            budget.setCategory("Shares");
            budget.setDate(share.getPurchaseDate());
            if (profit > 0) {
                budget.setType("profit");
            } else {
                budget.setType("loss");
            }

            budget.setAmount(Math.abs(share.getProfit()));

            budgetService.addBudgetLine(budget);
            sharesService.addShare(share);
            model.addAttribute("message", "New share saved");

            return "shares";
        }
    }



    @GetMapping("/user-shares")
    public ResponseEntity<List<Shares>> getUserShares() {
        String username = SecurityUtils.getAuthenticatedUsername();

        Users user = userRepository.findByUsername(username);

        List<Shares> userShares = sharesService.findByUserId(user.getId());
        return new ResponseEntity<>(userShares, HttpStatus.OK);
    }


}
