import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BenchmarkNumerosPrimos {
    public static boolean ehPrimo(long numero) {
        if (numero <= 1) return false;
        if (numero == 2) return true;
        if (numero % 2 == 0) return false;

        for (long i = 3; i <= numero / i; i += 2) {
            if (numero % i == 0) return false;
        }

        return true;
    }

    static class WorkerPrimos implements Callable<Long> {
        private final long inicio;
        private final long fim;
        private long contagemPrimos = 0;

        public WorkerPrimos(long inicio, long fim) {
            this.inicio = inicio;
            this.fim = fim;
        }

        @Override
        public Long call() {
            for (long i = inicio; i <= fim; i++) {
                if (ehPrimo(i)) {
                    contagemPrimos++;
                }
            }

            return contagemPrimos;
        }
    }

    public static void executarBenchmark(int quantidadeThreads, long limiteMaximo) throws InterruptedException {
        if (quantidadeThreads <= 0) {
            throw new IllegalArgumentException("A quantidade de threads deve ser maior que 0");
        }

        if (limiteMaximo < 1) {
            throw new IllegalArgumentException("O limite máximo deve ser maior ou igual a 1");
        }

        ExecutorService executor = Executors.newFixedThreadPool(quantidadeThreads);
        List<Future<Long>> tarefas = new ArrayList<>();

        long quantidadeTarefas = Math.max(quantidadeThreads * 4L, 1L);
        long tamanhoBloco = Math.max(1l,
                (limiteMaximo + quantidadeTarefas - 1) / quantidadeTarefas);

        long tempoInicio = System.currentTimeMillis();

        for (long inicio = 1; inicio <= limiteMaximo; inicio += tamanhoBloco) {
            long fim = Math.min(limiteMaximo, inicio + tamanhoBloco - 1);
            tarefas.add(executor.submit(new WorkerPrimos(inicio, fim)));
        }

        long totalNumerosPrimos = 0;
        try {
            for (Future<Long> tarefa : tarefas) {
                totalNumerosPrimos += tarefa.get();
            }
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Falha ao processar a tarefa", exception.getCause());
        } finally {
            executor.shutdown();
        }

        long tempoFim = System.currentTimeMillis();
        long tempoExecucao = tempoFim - tempoInicio;

        System.out.printf(
                "Threads: %2d | Tempo: %5d ms | Quantidade de números primos encontrados: %d%n",
                quantidadeThreads, tempoExecucao, totalNumerosPrimos
        );
    }

    public static void main(String[] args) throws InterruptedException {
        long limite = 10_000_000L;

        System.out.println("=== BENCHMARK DE PROGRAMAÇÃO PARALELA ===");
        System.out.println("Limite de busca: 1 até " + limite);
        System.out.println("----------------------------------------");

        int[] configuracaoThreads = {1, 2, 4, 8, 16};

        for (int numeroThreads : configuracaoThreads) {
            executarBenchmark(numeroThreads, limite);
        }
    }
}