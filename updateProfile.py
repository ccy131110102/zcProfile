import requests,json


with open('profile.json', 'r', encoding='utf8') as f:
    profile = json.loads(f.read())




profileContent = {
	"appId": "TEST2",
	"profileName": "TEST2_PROFILE",
	"profileContent": profile}

# device = {
#     "appId": 'TEST2',
#     "profileId": 'TEST2_PROFILE',
#     "deviceInfo": '',
#     "deviceId": "11"
# }

host = "http://58.59.64.11:8078/"

# r = requests.post(host + 'appResigter',headers={'Content-Type': 'application/json'}, json={'appName': 'TEST2'})
r = requests.post(host + 'profileRegister',headers={'Content-Type': 'application/json'}, json=profileContent)
# r = requests.post(host + 'deviceRegister',headers={'Content-Type': 'application/json'}, json=profileContent)


print(r.content)