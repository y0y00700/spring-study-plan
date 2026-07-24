package study.constructorinjection;

public class OrderService {
    PaymentProcessor processor;
    public OrderService(PaymentProcessor processor){
        this.processor = processor;
    }
    public String pay(){
        return processor.pay();
    }
}
