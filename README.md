# Umibench: testbench for 📏 factuality and 🎭 sentiment classifiers

## Function
Umibench compares the performance of various models on 2 tasks: detection of factuality (objectivity vs subjectivity) and detection of sentiment (positive, negative or neutral).

It generates a leaderboard which is visible at the bottom of this page.

## How to add a model or a dataset to this leaderboard? Just provide a link!

I will take care of everything. Please send:

- for a labelled dataset: a link to a place where I can download it.
- for a model: a url to the API of the model. If an API key is necessary, please provide it. Otherwise I will do my best to run it at my own expense.

## Which models are tested so far?

1. Thesis_Titan ([paper](https://ceur-ws.org/Vol-3497/paper-020.pdf),  [api](https://huggingface.co/GroNLP/mdebertav3-subjectivity-english))
2. TimeLMs ([paper](https://arxiv.org/abs/2202.03829),  [api](https://huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest))
3. gpt-3.5-turbo-basic-prompt ([paper](https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates),  [api](https://api.openai.com/v1/chat/completions))
4. OpenHermes-2-Mistral-7B-advanced-prompt ([paper](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B),  [api](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B))
5. gpt-3.5-turbo-advanced-prompt ([paper](https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates),  [api](https://api.openai.com/v1/chat/completions))
6. OpenHermes-2-Mistral-7B-basic-prompt ([paper](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B),  [api](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B))
7. umigon ([paper](https://aclanthology.org/S13-2068/no),  [api](https://nocodefunctions.com/umigon/sentiment_analysis_tool.html))

[Supplementary information on these models](supplementary_info_on_models.md)

## Against which annotated datasets are these models tested?


1. mpqa ([paper](https://doi.org/10.1007/s10579-005-7880-9),  [data source](https://mpqa.cs.pitt.edu/))<br/><span style="font-size: .8rem">a set of newswire articles on international politics annotated for factuality and sentiment</span><br/>
2. kaggle-headlines ([paper](https://arxiv.org/abs/2209.11429),  [data source](https://www.kaggle.com/datasets/rmisra/news-category-dataset?resource=download))<br/><span style="font-size: .8rem">a set of headlines from US newspapers annotated for factuality</span><br/>
3. xfact ([paper](http://dx.doi.org/10.18653/v1/2021.acl-short.86),  [data source](https://github.com/utahnlp/x-fact))<br/><span style="font-size: .8rem">a database of factual statements labeled for veracity by expert fact-checkers</span><br/>
4. subjqa ([paper](http://dx.doi.org/10.18653/v1/2020.emnlp-main.442),  [data source](https://huggingface.co/datasets/subjqa))<br/><span style="font-size: .8rem">a set of consumer reviews on goods from the “electronics” product category</span><br/>
5. apple ([paper](https://oro.open.ac.uk/40660/),  [data source](https://github.com/seinecle/twitter_corpus_sanders_analytics))<br/><span style="font-size: .8rem">a set of tweets mentioning Apple and annotated for neutral, positive and negative sentiment</span><br/>
6. alexa ([paper](https://arxiv.org/abs/2110.05456),  [data source](https://github.com/alexa/factual-consistency-analysis-of-dialogs/))<br/><span style="font-size: .8rem">a set of factual statements curated by the Amazon Alexa development team</span><br/>
7. carblacac ([paper](https://huggingface.co/datasets/carblacac/twitter-sentiment-analysis),  [data source](https://huggingface.co/datasets/carblacac/twitter-sentiment-analysis))<br/><span style="font-size: .8rem">a set of 200 tweets from anonymous individuals on their daily lives annotated for negative and positive sentiment</span><br/>
8. clef2023 ([paper](https://doi.org/10.1007/978-3-031-42448-9),  [data source](https://gitlab.com/checkthat_lab/clef2023-checkthat-lab/-/tree/main/task2/data/subtask-2-english))<br/><span style="font-size: .8rem">a set of phrases extracted from news articles annotated for factuality and subjectivity</span><br/>

Find [supplementary information on each dataset here](supplementary_info_on_annotated_datasets.md)

# Leaderboard

## Factuality: differentiating objective from subjective statements

*Umigon and TimeLMs are models for sentiment analysis. We test them on factuality by considering that a prediction for "neutral sentiment" is equivalent to an* ***objective*** *statement, while a predicition for a positive or negative sentiment is equivalent to predicting a* ***subjective*** *statement.*

Weighted F1 values:




|                                                                                                   | [alexa](https://github.com/alexa/factual-consistency-analysis-of-dialogs/) | [apple](https://github.com/seinecle/twitter_corpus_sanders_analytics) | [carblacac](https://huggingface.co/datasets/carblacac/twitter-sentiment-analysis) | [clef2023](https://gitlab.com/checkthat_lab/clef2023-checkthat-lab/-/tree/main/task2/data/subtask-2-english) | [kaggle-headlines](https://www.kaggle.com/datasets/rmisra/news-category-dataset?resource=download) | [mpqa](https://mpqa.cs.pitt.edu/) | [subjqa](https://huggingface.co/datasets/subjqa) | [xfact](https://github.com/utahnlp/x-fact) |
| ------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- | --------------------------------------------------------------------- | --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------- | --------------------------------- | ------------------------------------------------ | ------------------------------------------ |
| [OpenHermes-2-Mistral-7B-advanced-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B) | 0,986                                                                      | 0,609                                                                 | 0,961                                                                             | 0,641                                                                                                        | 0,738                                                                                              | 0,784                             | 0,974                                            | 0,792                                      |
| [OpenHermes-2-Mistral-7B-basic-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B)    | 0,969                                                                      | 0,707                                                                 | 0,916                                                                             | 0,563                                                                                                        | 0,544                                                                                              | 0,678                             | 0,883                                            | 0,388                                      |
| [Thesis_Titan](https://ceur-ws.org/Vol-3497/paper-020.pdf)                                        | 0,964                                                                      | 0,612                                                                 | 0,860                                                                             | 0,821                                                                                                        | 0,857                                                                                              | 0,877                             | 0,789                                            | 0,960                                      |
| [TimeLMs](https://arxiv.org/abs/2202.03829)                                                       | 0,872                                                                      | 0,614                                                                 | 0,956                                                                             | 0,610                                                                                                        | 0,719                                                                                              | 0,706                             | 0,948                                            | 0,671                                      |
| [umigon](https://aclanthology.org/S13-2068/no)                                                    | 0,960                                                                      | 0,656                                                                 | 0,788                                                                             | 0,606                                                                                                        | 0,945                                                                                              | 0,783                             | 0,957                                            | 0,978                                      |
### Overall scores and leaderboard for models on factuality task
The values for each model are the sums of the weighted F1 scores for each dataset, weighted by the number of entries of each dataset.

|               | [umigon](https://aclanthology.org/S13-2068/no) | [Thesis_Titan](https://ceur-ws.org/Vol-3497/paper-020.pdf) | [OpenHermes-2-Mistral-7B-advanced-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B) | [TimeLMs](https://arxiv.org/abs/2202.03829) | [OpenHermes-2-Mistral-7B-basic-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B) |
| ------------- | ---------------------------------------------- | ---------------------------------------------------------- | ------------------------------------------------------------------------------------------------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| overall score | 0,907                                          | 0,906                                                      | 0,790                                                                                             | 0,699                                       | 0,517                                                                                          |
| rank          | 1                                              | 2                                                          | 3                                                                                                 | 4                                           | 5                                                                                              |

## Sentiment: differentiating between positive sentiment, negative sentiment and neutral sentiment

*here we do not test the model "Thesis Titan" as it is a model for the task of factuality categorization, which cannot be extended or adapted to sentiment analysis*

*also, the models are tested against just one dataset: MPQA. The reason is that AFAIK this is the only annotated dataset in existence which makes a rigorous distinction between different sentiment valences* ***all while annotating texts for their subjective or objective character*** 

Weighted F1 values:


|                                                                                                    | [apple](https://github.com/seinecle/twitter_corpus_sanders_analytics) | [carblacac](https://huggingface.co/datasets/carblacac/twitter-sentiment-analysis) | [mpqa](https://mpqa.cs.pitt.edu/) |
| -------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | --------------------------------------------------------------------------------- | --------------------------------- |
| [OpenHermes-2-Mistral-7B-advanced-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B)  | 0,598                                                                 | 0,867                                                                             | 0,847                             |
| [OpenHermes-2-Mistral-7B-basic-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B)     | 0,690                                                                 | 0,883                                                                             | 0,708                             |
| [TimeLMs](https://arxiv.org/abs/2202.03829)                                                        | 0,623                                                                 | 0,862                                                                             | 0,762                             |
| [gpt-3.5-turbo-advanced-prompt](https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates) | 0,724                                                                 | 0,820                                                                             | 0,867                             |
| [gpt-3.5-turbo-basic-prompt](https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates)    | 0,663                                                                 | 0,832                                                                             | 0,827                             |
| [umigon](https://aclanthology.org/S13-2068/no)                                                     | 0,614                                                                 | 0,700                                                                             | 0,862                             |
### Overall scores and leaderboard for models on sentiment task
The values for each model are the sums of the weighted F1 scores for each dataset, weighted by the number of entries of each dataset.

|               | [gpt-3.5-turbo-advanced-prompt](https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates) | [umigon](https://aclanthology.org/S13-2068/no) | [OpenHermes-2-Mistral-7B-advanced-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B) | [gpt-3.5-turbo-basic-prompt](https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates) | [TimeLMs](https://arxiv.org/abs/2202.03829) | [OpenHermes-2-Mistral-7B-basic-prompt](https://huggingface.co/teknium/OpenHermes-2-Mistral-7B) |
| ------------- | -------------------------------------------------------------------------------------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| overall score | 0,832                                                                                              | 0,798                                          | 0,791                                                                                             | 0,790                                                                                           | 0,735                                       | 0,712                                                                                          |
| rank          | 1                                                                                                  | 2                                              | 3                                                                                                 | 4                                                                                               | 5                                           | 6                                                                                              |
## How to run it
Umibench is programmed in Java.

- you need Java 17 or later.
- clone this repo, open it in your fav IDE
- in the directory `private`, rename `example-properties.txt` to `properties.txt` and change the API keys in it.
- navigate to the package `src/main/java/net/clementlevallois/umigon/eval/models` and open the classes where the API calls to Huggingface are made. Replace the endpoints with the endpoints of the models you want to test. Public endpoints don't have enough capacity. You need to spin your own endpoints.
- run the main class of the project (`Controller.java`)

## Why factuality and sentiment compared in the same test bench?
Most if not all models for sentiment analysis classify the 2 following statements as being SUBJECTIVE / NEGATIVE, which I put in question:

- "State terrorism is a political concept": should be OBJECTIVE, NOT SUBJECTIVE / NEGATIVE
- "State terrorism is not the best course of action": should be SUBJECTIVE / NEGATIVE

This distinction, which can appear as a subtle one, is actually very important to maintain when it comes to the analysis of discourse in media, politics and culture in general. Otherwise, the examination of opinions and debates on topics ladden with a positive or negative *factual* valence will be tainted by this lack of a distinction.

I developed Umigon, a model for sentiment analysis, which thrives to maintain this distinction. Annotated datasets which would include labels on whether the text entries are objective from the point of view of the locutor are rare. Actually, only the MPQA dataset (listed above) maintains this distinction.

To extend the range of tests, I have included datasets which are carefully annotated for factuality: objective or subjective? The expectation is that a good model in sentiment analysis (maintaining the distinction made above) has to perform well on this factuality test as well - otherwise this means that its definition of "sentiment" makes a confusion between actual sentiment, and positively or negatively ladden factuals.

## Contact
Clement Levallois, levallois@em-lyon.com



_This readme file and the leaderboard it includes has been generated on 2024-02-27T16:17:58.352201800_
