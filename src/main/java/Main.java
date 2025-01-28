import java.time.LocalDate;


public class Main {
    public static void main(String[] args) {

     /* Stock berkshire = new Stock("버크셔",455.03,4, LocalDate.of(2024,9,25));
        berkshire.howLongInvesting();
        //berkshire.additionalBuy(449.5,1);
        berkshire.targetPrice(20);*/
        InvestingService is = new InvestingService();
        is.start();
    }
}