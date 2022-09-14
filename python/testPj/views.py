import json
import os
from django.http.response import JsonResponse
from django.views.decorators.csrf import csrf_exempt

from janome.tokenizer import Tokenizer

udf = './user_defined_dictionary.csv'
size = os.path.getsize(udf)

if size > 0:
    print("loading " + udf + "  " + str(size) + " bytes")
    tokenizer = Tokenizer(udf, udic_enc='utf8')
else:
    print("skip to load " + udf)
    tokenizer = Tokenizer()

"""
https://mocobeta.github.io/janome/
"""
@csrf_exempt
def index(request):

    if request.method == 'GET':
        return JsonResponse({})

    input = json.loads(request.body)
    word = input["word"]

    tokens = []
    for token in tokenizer.tokenize(word, wakati=True):
        tokens.append(token)

    return JsonResponse({"tokens": tokens})