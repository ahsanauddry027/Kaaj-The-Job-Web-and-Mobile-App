package app.kaaj.shared.internal;

import app.kaaj.shared.Taxonomy;
import app.kaaj.shared.Term;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Open to everyone: find occupations and skills by their Bangla, English or Banglish names. */
@RestController
@RequestMapping("/api/v1/taxonomy")
class TaxonomyController {

    private final Taxonomy taxonomy;

    TaxonomyController(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    @GetMapping("/occupations")
    List<Term> occupations(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "20") int limit) {
        return taxonomy.searchOccupations(q, limit);
    }

    @GetMapping("/skills")
    List<Term> skills(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "20") int limit) {
        return taxonomy.searchSkills(q, limit);
    }
}
