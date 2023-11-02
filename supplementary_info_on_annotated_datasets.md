# Supplementary information on the datasets

The overall intention is to create a collection of datasets which:

- are diverse in terms of their domains (news, reviews, headlines, etc.)
- can be labelled according to precise standards of what subjectivity / factuality / sentiment is.

In particular, we did not include datasets the subjective vs factuality dimension in a principled way: for instance, the classic [subjectivity dataset by Pang and Lee](https://www.cs.cornell.edu/people/pabo/movie-review-data/) includes many subjective statements in the "plot" (objective) category. 


## [Thesis Titan - at the Clef2023 conference](https://checkthat.gitlab.io/clef2023/)
The corpus is annoted for OBJECTIVITY vs SUBJECTIVITY.
Corpus in English of subtask-2-english: dev_en.tsv *and* train_en.tsv datasets.


## [News Category Dataset](https://arxiv.org/abs/2209.11429) - [Kagggle link](https://www.kaggle.com/datasets/rmisra/news-category-dataset?resource=download)
The corpus is annoted for OBJECTIVITY vs SUBJECTIVITY.

1,000 headlines extracted at random. I then annotated them for factuality vs subjectivity, as manual inspection revealed that a few headlines could be said to be subjective rather than factual (eg, "Reporter Gets Adorable Surprise From Her Boyfriend While Live On TV" -> coded as "subjective" because of the token "adorable")

## [MPQA dataset (1.2)](https://mpqa.cs.pitt.edu/corpora/mpqa_corpus/mpqa_corpus_1_2/)
The corpus is annoted for OBJECTIVITY vs SUBJECTIVITY. Subjective statements are annotated for POSITIVE, NEGATIVE OR NEUTRAL sentiment.

- Entries labelled as "subjective" are the ones annotated with the following annotations: GATE_expressive-subjectivity AND nested-source="w"
- Within the "subjective" entries, sentiment was characterized based on the annotation "polarity", which takes the following values: "positive", "negative", "neutral". 
- Entries labelled as "objective" are the ones annotated with the following tag: GATE_objective-speech-event. All "objective" entries were marked as "neutral" for sentiment.
- In all cases, an entry was not included in the dataset if it included the following annotation: polarity="uncertain"

## [SubjQA: A Dataset for Subjectivity and Review Comprehension](https://aclanthology.org/2020.emnlp-main.442/)
The corpus is annoted for OBJECTIVITY vs SUBJECTIVITY. In practice, all entries are labelled as SUBJECTIVE.

In the dataset, we selected the "electronics" product category from the "train" set and filtered the data entries to keep only those which were rated for maximum subjectivity (score of 1 out of 5 in the field "ans_subj_score").
This returns 1,373 records out of 2,346.

## [X-FACT: A New Benchmark Dataset for Multilingual Fact Checking](https://aclanthology.org/2021.acl-short.86/)
The corpus is annoted for OBJECTIVITY vs SUBJECTIVITY. In practice, all entries are labelled as OBJECTIVE.

The entries were extracted from the field "claim" from the entries in English in the file "train.all.tsv" in the "x-fact/data/x-fact-including-en/" directory.

## [Alexa: Factual consistency analysis of dialogs](https://arxiv.org/abs/2110.05456)

Data available at this repo: https://github.com/alexa/factual-consistency-analysis-of-dialogs/

This is a project by the Alexa research team on identifying factual correctness.

In the file factual_dataset_expert.csv; the field "knowledge" contains factual statements derived from the field "context"

Entries in this field are objective by definition, not subjective.

In practice, I extracted all the entries of the "knowledge" field, with filters on two other fields:

- "hallucination" = No
- "verifiable" = y (yes)

This led to 470 entries, all labelled as "Objective"