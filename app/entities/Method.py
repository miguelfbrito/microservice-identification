class Method:

    def __init__(self, name, parameters_data_type, return_data_type):
        self.name = name
        self.parameters_data_type = parameters_data_type
        # List<String, List<Integer>> -> [String, Integer]
        self.return_data_type = return_data_type

    def total_words(self, array):
        return sum(len(sentence.split()) for sentence in array)

    def __str__(self):
        return f"({self.name}, {self.parameters_data_type}, {self.return_data_type})"

    def __repr__(self):
        return f"({self.name}, {self.parameters_data_type}, {self.return_data_type})"
