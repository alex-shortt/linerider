public class LineRider {
    public static void main(String args[]) {
        Ticker ticker = new Ticker(20); // 20 ticks per second

        ticker.addTickListener(new TickListener() {

            @Override
            public void onTick(float deltaTime) {
                System.out.println(String.format("Ticked with deltaTime %f", deltaTime));
            }
        });
        
        while (true) {
            ticker.update();
        }
    }
}
