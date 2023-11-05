# Supplementary information on models

## Umigon
Disclosure: Umigon is developed by the author of this testbench.
A model dedicated to evaluating the sentiment in texts. Umigon is based on lexicons + heuristics to detect expressions which reflect a sentiment, while taking the context of use into account.

## TimeLMs
A model dedicated to evaluating the sentiment in texts.From the repo on HuggingFace: "This is a roBERTa-base model trained on ~58M tweets and finetuned for sentiment analysis with the TweetEval benchmark. This model is suitable for English".

## Open Hermes 2 Mistral 7b - basic prompt
A large language model with broad capabilities. From the repo on HuggingFace: "OpenHermes 2 Mistral 7B is a state of the art Mistral Fine-tune. OpenHermes was trained on 900,000 entries of primarily GPT-4 generated data, from open datasets across the AI landscape. [More details soon]".

I thank [Alexander Doria](https://www.linkedin.com/in/pierre-carl-langlais-b0105b10) for [running the MPQA dataset on it - see thread](https://twitter.com/seinecle/status/1720418976243515418), on November 3, 2023.

The prompt was:

>You are the equivalent of a human annotator. You must:
>- label the sentiment of the text provided below. The label should be a single word: "positive", "negative" or "neutral".
>- extract the unique identifier of the text. The unique identifier is the string of characters that is at the start of the text, up to the # character.
>
>Your response should be exactly the unique identifier of the text, followed by a space, followed by the label. Do not add the text itself or anything else.
>
>The text: 


## Thesis Titan
A model dedicated to evaluating the factuality of a text. From the repo on HuggingFace: "Fine-tuned mDeBERTa V3 model for subjectivity detection in newspaper sentences. This model was developed as part of the CLEF 2023 CheckThat! Lab Task 2: Subjectivity in News Articles." "The model ranked third in the CheckThat! Lab and obtained a macro F1 of 0.77 and a SUBJ F1 of 0.79."

## GPT3 turbo - basic prompt
A large language model with broad capabilities. 

It runs on the MPQA dataset (3,198 entries) for less than a dollar, in approximately 12 hours. Yes it is that slow! Less than 5 entries generated an error and were lost.

The prompt was:

>Role "system" : You are a the equivalent of a human annotator in a data labelling task. The task consists in labelling the sentiment of a text provided by the user. The label should be a single word: "positive", "negative" or "neutral".
>
>Role "User" = "The text to label: \n\n" + text to label


## GPT3 turbo - advanced prompt
Same as previous, but the prompt is more elaborated:

>Role "system" : You are a the equivalent of a human annotator in a data labelling task. The task consists in labelling the sentiment of a text provided by the user. When annotating, be especially attentive to these 3 recommendations:
>1. you should annotate the sentiment expressed by the author of the text, not the sentiment expressed by a person cited in the text.
>2. a sentiment is expressed when the text reflects personal feelings, tastes, or opinions.
>3. a factual, even when it has strong positive or negative prior associations (such as "war" or "happyness"), is not a sentiment.
>
>The label should be a single word: "positive", "negative" or "neutral".
>
>Role "User" = "The text to label: \n\n" + text to label




