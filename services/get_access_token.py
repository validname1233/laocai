from pydantic import BaseModel, Field
import requests


class Response(BaseModel):
    access_token: str = Field(..., description="获取到的凭证")
    expires_in: int = Field(..., description="凭证有效时间，单位：秒。目前是7200秒之内的值")


def get_access_token(appid, bot_secret):
    response = Response(**requests.post("https://bots.qq.com/app/getAppAccessToken",
                                        headers={"Content-Type": "application/json"},
                                        json={
                                            "appId": appid,
                                            "clientSecret": bot_secret
                                        }).json())
    return response.access_token
