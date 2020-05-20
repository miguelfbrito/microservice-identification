class Method:

    def __init__(self, name="FailedToLoadMethodName", parameters_data_type=[], return_data_type=[]):
        self.name = name
        self.parameters_data_type = parameters_data_type
        self.return_data_type = return_data_type

    def get_merge_of_entities(self):
        name_weight = 3
        parameters_weight = 1
        return_weight = 3

        string = parameters_weight * self.parameters_data_type + \
            return_weight * self.return_data_type

        string = " ".join(string) + name_weight * (" " + self.name)

        # print(f"Merge of entities method {' '.join(string)}")

        return string

    def __str__(self):
        return f"({self.name}, {self.parameters_data_type}, {self.return_data_type})"

    def __repr__(self):
        return f"({self.name}, {self.parameters_data_type}, {self.return_data_type})"
