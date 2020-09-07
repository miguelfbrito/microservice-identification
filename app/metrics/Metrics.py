from Settings import Settings


class Metrics:

    def __init__(self, metric_executor=None, clusters_results=[]):
        super().__init__()
        self.metric_executor = metric_executor
        self.clusters_results = clusters_results
        self.metrics = []

    def calculate(self):
        # TODO: Ugly, refactor into cleaner approach
        chm, chd, ifn, irn, opn, smq, scoh, scop, cmq, ccoh, ccop = self.metric_executor.run_metrics()
        self.metrics.append((chm, chd, ifn, irn, opn, smq,
                             scoh, scop, cmq, ccoh, ccop))

    def save(self):

        # TODO: Ugly, refactor into cleaner approach
        resolution = []
        chm = []
        chd = []
        ifn = []
        irn = []
        opn = []
        smq = []
        scoh = []
        scop = []
        cmq = []
        ccoh = []
        ccop = []
        services_length = []

        metric_path = f"{Settings.DIRECTORY}/data/metrics/{Settings.PROJECT_NAME}_{Settings.ID}_K{Settings.K_TOPICS}.csv"
        print(f"\n\nMetric Path: {metric_path}")
        with open(metric_path, 'w+') as f:
            # TODO: Ugly, refactor into cleaner approach
            for cluster_result, metric in zip(self.clusters_results, self.metrics):
                chm.append(metric[0])
                chd.append(metric[1])
                ifn.append(metric[2])
                irn.append(metric[3])
                opn.append(metric[4])
                smq.append(metric[5])
                scoh.append(metric[6])
                scop.append(metric[7])
                cmq.append(metric[8])
                ccoh.append(metric[9])
                ccop.append(metric[10])
                services_length.append(cluster_result)

                resolution.append(round(cluster_result[2], 2))

                print(f"CLUSTER RESULT:: {cluster_result}")

                line = f"{round(cluster_result[2], 2)},{metric[0]},{metric[1]},{metric[2]},{metric[3]},{metric[4]},{metric[5]},{metric[6]},{metric[7]},{metric[8]},{metric[9]},{metric[10]}, {len(cluster_result[0])}"
                f.write(f"{line}\n")

                # average_cluster_len = sum(
                # x for x in cluster_result.values()) / len(cluster_result[0])
                # print(f"Average cluster len {average_cluster_len}")

                total = metric[0] + metric[1] + metric[5] + metric[6]
                print(
                    f"Sum for resolution {round(cluster_result[2], 2)} -> {round(total,2)}")

        # plt = build_plot(resolution, chm, chd, ifn, smq, cmq, irn, opn)
        # plt.savefig(
        #    f"{Settings.DIRECTORY}/data/metrics/images/{Settings.PROJECT_NAME}_{Settings.ID}_K{Settings.K_TOPICS}.png")
        # plt.show()

    def set_metric_executor(self, metric_executor):
        self.metric_executor = metric_executor

    def set_cluster_results(self, clusters_results):
        self.clusters_results = clusters_results
