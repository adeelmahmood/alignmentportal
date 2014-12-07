package org.jhu.metagenomics.alignmentportal.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.ReferenceBound;
import com.google.api.services.genomics.model.SearchVariantsRequest;
import com.google.api.services.genomics.model.SearchVariantsResponse;
import com.google.api.services.genomics.model.VariantSet;

@RestController
@RequestMapping("/variants")
public class VariantsController {

	private static final int PAGE_SIZE = 10;

	@Value("${google.genomics.project.number}")
	private long projectNumber;

	private final Genomics genomics;

	@Autowired
	public VariantsController(Genomics genomics) {
		this.genomics = genomics;
	}

	@RequestMapping(value = "/list/{variantSetId}", method = RequestMethod.GET)
	public Map<String, Object> list(@PathVariable String variantSetId,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end,
			@RequestParam(required = false) String referenceName,
			@RequestParam(required = false) String nextPageToken)
					throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		
		//get variant set
		VariantSet variantSet = genomics.variantsets().get(variantSetId).execute();
		data.put("variantSetId", variantSet.getId());
		data.put("variantSetReferenceName", getReferenceName(variantSet.getReferenceBounds()));
		
		//get variants
		SearchVariantsRequest req = new SearchVariantsRequest()
								.setVariantSetIds(Arrays.asList(variantSetId))
								.setPageSize(PAGE_SIZE)
								.setReferenceName(referenceName)
								.setPageToken(nextPageToken);
		if(start != null) {
			req.setStart(start);
		}
		if(end != null) {
			req.setEnd(end);
		}
		SearchVariantsResponse resp = genomics.variants().search(req).execute();
		data.put("variants", resp.getVariants());
		data.put("previousPageToken", nextPageToken);
		data.put("nextPageToken", resp.getNextPageToken());
		return data;
	}
	
	private String getReferenceName(List<ReferenceBound> referenceBounds) {
		String referenceName = "";
		if(referenceBounds != null && referenceBounds.size() > 0) {
			referenceName = referenceBounds.get(0).getReferenceName();
		}
		return referenceName;
	}
}