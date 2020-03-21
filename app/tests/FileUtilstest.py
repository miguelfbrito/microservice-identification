import unittest
from FileUtils import FileUtils


class FileUtilsTest(unittest.TestCase):

    def setUp(self):
        self.source_code = """
        package org.springframework.samples.petclinic.vet;

        import org.springframework.stereotype.Controller;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.ResponseBody;

        import java.util.Map;

        /**
        * @author Juergen Hoeller
        * @author Mark Fisher
        * @author Ken Krebs
        * @author Arjen Poutsma
        */


        @Controller
        class VetController {

            private final VetRepository vets;

            public VetController(VetRepository clinicService) {
                this.vets = clinicService;
            }

            /* this is 
            * another comment
            */

            @GetMapping("/vets.html")
            public String showVetList(Map<String, Object> model) {
                // Here we are returning an object of type 'Vets' rather than a collection of Vet
                // objects so it is simpler for Object-Xml mapping
                Vets vets = new Vets();
                vets.getVetList().addAll(this.vets.findAll());
                model.put("vets", vets);
                return "vets/vetList";
            }
        """

    def test_extract_comments_from_string(self):

        comments = FileUtils.extract_comments_from_string(self.source_code)
        expected_comments = ["@author Juergen Hoeller @author Mark Fisher @author Ken Krebs @author Arjen Poutsma", "this is another comment",
                             "Here we are returning an object of type 'Vets' rather than a collection of Vet", 'objects so it is simpler for Object-Xml mapping']

        self.assertEqual(comments, expected_comments)


if __name__ == '__main__':
    unittest.main()
