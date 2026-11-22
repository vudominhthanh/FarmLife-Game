using UnityEngine;
using UnityEngine.UI;

public class DragIconUI : MonoBehaviour
{
    public static DragIconUI Instance;
    private Image iconImage;


    private void Awake()
    {
        Instance = this;
        iconImage = GetComponent<Image>();
        gameObject.SetActive(false);
    }

    public void Show(Sprite sprite)
    {
        iconImage.sprite = sprite;
        iconImage.enabled = true;
        gameObject.SetActive(true);
    }

    public void Hide()
    {
        iconImage.enabled = false;
        gameObject.SetActive(false);
    }

    private void Update()
    {
        if (gameObject.activeSelf)
        {
            transform.position = Input.mousePosition;
        }
    }
}
